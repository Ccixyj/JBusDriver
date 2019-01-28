package me.jbusdriver.ui.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.util.ArrayMap
import com.billy.cc.core.component.CCUtil.put
import com.google.gson.JsonObject
import com.tbruyelle.rxpermissions2.RxPermissions
import com.umeng.analytics.MobclickAgent
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.R
import me.jbusdriver.base.*
import me.jbusdriver.base.common.BaseActivity
import me.jbusdriver.base.common.C
import me.jbusdriver.http.GitHub
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.ui.data.enums.DataSourceType
import org.jsoup.Jsoup

class SplashActivity : BaseActivity() {

    private var urls = arrayMapof<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        immersionBar.transparentBar().init()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        init()
    }

    private fun init() {
        RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .flatMap {
                    initUrls()
                }
                .doOnError {
                    KLog.e("获取可用url错误 :$it")
                    MobclickAgent.reportError(viewContext, it)
                    CacheLoader.acache.remove(C.Cache.BUS_URLS)
                }
                .retry(1)
                .doFinally {
                    postMain {
                        toast("load url : ${JAVBusService.defaultFastUrl}")
                        MainActivity.start(this)
                        finish()
                    }.addTo(rxManager)
                }
                .subscribeBy(onNext = {
                    it.get(DataSourceType.CENSORED.key)?.let {
                        JAVBusService.defaultFastUrl = it.urlHost
                    }
                }, onError = {
                    it.printStackTrace()
                    KLog.w("init urls error : $it")
                }, onComplete = {
                })
                .addTo(rxManager)

    }

    private fun initUrls(): Observable<ArrayMap<String, String>> {
        return if (CacheLoader.lru.get(C.Cache.BUS_URLS).isNullOrBlank()) {
            //内存在没有地址时 ,先从disk获取缓存的,没有则从网络下载
            val urlsFromDisk = CacheLoader.justDisk(C.Cache.BUS_URLS).map {
                GSON.fromJson<ArrayMap<String, String>>(it)
            }
            val urlsFromUpdateCache = Flowable.concat(CacheLoader.justLru(C.Cache.ANNOUNCE_URL), GitHub.INSTANCE.announceWithPlugin().addUserCase(4)).firstOrError().toFlowable()
                    .map { source ->
                        //放入内存缓存,更新需要
                        val r = GSON.fromJson<JsonObject>(source) ?: JsonObject()
                        CacheLoader.cacheLru(C.Cache.ANNOUNCE_VALUE to r)
                        arrayMapof<String, String>().apply {
                            val xyzLoader = r.getAsJsonObject("xyzLoader") ?: JsonObject()
                            JAVBusService.defaultXyzUrl = xyzLoader.get("url")?.asString?.removeSuffix("/").orEmpty()
                            JAVBusService.xyzHostDomains.addAll(xyzLoader.getAsJsonArray("legacyHost")?.map { it.asString }
                                    ?: emptyList())
                            val availableUrls = r.get("backUp")?.asJsonArray
                            //赋值一个默认的(随机)
                            availableUrls?.let {
                                it.mapNotNull { it.asString }.shuffled().firstOrNull()?.let {
                                    JAVBusService.defaultFastUrl = it
                                    urls[DataSourceType.CENSORED.key] = it
                                }
                            }
                            put(DataSourceType.CENSORED.key, availableUrls.toString())
                            KLog.d("init urls first :$source for $this")
                        }
                    }
                    .flatMap {
                        urls = it
                        val mapFlow = GSON.fromJson<List<String>>(it[DataSourceType.CENSORED.key]
                                ?: "").map {
                            Flowable.combineLatest(Flowable.just<String>(it),
                                    JAVBusService.INSTANCE.get(it).addUserCase(15).onErrorReturnItem(""),
                                    BiFunction<String, String?, Pair<String, String>> { t1, t2 -> t1 to t2 })
                        }
                        Flowable.mergeDelayError(mapFlow).filter { it.second.isNotBlank() }.take(1)
                    }
                    .firstOrError()
                    .doOnError { CacheLoader.acache.remove(C.Cache.ANNOUNCE_URL) }
                    .map {
                        val ds = DataSourceType.values().takeLast(DataSourceType.values().size - 1).toMutableList()
                        Jsoup.parse(it.second).select(".navbar-nav a").forEach { box ->
                            ds.find { box.text() == it.key }?.let {
                                ds.remove(it)

                                urls.put(it.key, box.attr("href").removeSuffix("/"))
                            }
                        }
                        urls[DataSourceType.XYZ.key]?.let {
                            //欧美
                            urls[DataSourceType.XYZ_ACTRESSES.key] = "$it/${DataSourceType.XYZ_ACTRESSES.key.split("/").last()}"
                            urls.put(DataSourceType.XYZ_GENRE.key, "$it/${DataSourceType.XYZ_GENRE.key.split("/").last()}")
                        }
                        urls[DataSourceType.CENSORED.key] = it.first

                        //change xyz
                        if (JAVBusService.defaultXyzUrl.isNotBlank()) {
                            urls[DataSourceType.XYZ.key] = JAVBusService.defaultXyzUrl
                            urls[DataSourceType.XYZ_ACTRESSES.key] = "${JAVBusService.defaultXyzUrl}/actresses"
                            urls[DataSourceType.XYZ_GENRE.key] = "${JAVBusService.defaultXyzUrl}/genre"
                        } else {
                            val host = JAVBusService.xyzHostDomains.firstOrNull() ?: "work"
                            val baseUrlSuffix = urls[DataSourceType.XYZ.key]?.substringAfterLast(".").orEmpty()
                            urls[DataSourceType.XYZ.key] = urls[DataSourceType.XYZ.key]?.replace(baseUrlSuffix, host)
                            urls[DataSourceType.XYZ_ACTRESSES.key] = urls[DataSourceType.XYZ_ACTRESSES.key]?.replace(baseUrlSuffix, host)
                            urls[DataSourceType.XYZ_GENRE.key] = urls[DataSourceType.XYZ_GENRE.key]?.replace(baseUrlSuffix, host)
                        }

                        CacheLoader.cacheLruAndDisk(C.Cache.BUS_URLS to urls, C.Cache.DAY * 2) //缓存所有的urls
                        CacheLoader.lru.put(DataSourceType.CENSORED.key + "false", it.second) //默认有种的
                        KLog.d("init urls second :$urls ")
                        urls
                    }.toFlowable()
            return Flowable.concat<ArrayMap<String, String>>(urlsFromDisk, urlsFromUpdateCache)
                    .firstElement().toObservable()
                    .subscribeOn(Schedulers.io())
        } else CacheLoader.justLru(C.Cache.BUS_URLS).map {
            GSON.fromJson<ArrayMap<String, String>>(it)
        }.toObservable()
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, SplashActivity::class.java))
        }
    }
}
