package me.jbusdriver.ui.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.util.ArrayMap
import com.cfzx.utils.CacheLoader
import com.google.gson.JsonObject
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.common.*
import me.jbusdriver.http.GitHub
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.ui.data.DataSourceType
import org.jsoup.Jsoup

class SplashActivity : BaseActivity() {

    var urls = arrayMapof<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        init()
    }

    private fun init() {
        Observable.combineLatest<Boolean, ArrayMap<String, String>, String>(RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                initUrls(), BiFunction { t1, t2 -> t2.values.firstOrNull() ?: "" })
                .doOnError { CacheLoader.acache.remove(C.Cache.BUS_URLS) }
                .retry(1)
                .doFinally {
                    KLog.d("doFinally")
                    AndroidSchedulers.mainThread().scheduleDirect {
                        toast("load url : ${JAVBusService.defaultFastUrl}")
                        MainActivity.start(this)
                        finish()
                    }
                }
                .subscribeBy(onNext = {
                    KLog.i("success : $it")
                    JAVBusService.defaultFastUrl = it.urlHost
                }, onError = {
                    it.printStackTrace()
                })
                .addTo(rxManager)

        Handler().postDelayed({
            KLog.d("clear dispose")
            rxManager.clear()
        }, 6000)
    }

    private fun initUrls(): Observable<ArrayMap<String, String>> {
        return if (CacheLoader.lru.get(C.Cache.BUS_URLS).isNullOrBlank()) {
            KLog.d("load initUrls")
            //内存在没有地址时 ,先从disk获取缓存的,没有则从网络下载
            val urlsFromDisk = CacheLoader.justDisk(C.Cache.BUS_URLS).map {
                AppContext.gson.fromJson<ArrayMap<String, String>>(it).apply {
                    KLog.i("map urls $this")
                }
            }
            val urlsFromNet = Flowable.concat(CacheLoader.justLru(C.Cache.ANNOUNCE_URL), GitHub.INSTANCE.announce().addUserCase()).firstOrError().toFlowable()
                    .map {
                        source ->
                        //放入内存缓存,更新需要
                        CacheLoader.cacheLru(C.Cache.ANNOUNCE_VALUE to source)
                        arrayMapof<String, String>().apply {
                            put(DataSourceType.CENSORED.key, AppContext.gson.fromJson<JsonObject>(source)?.get("backUp")?.asJsonArray.toString())
                        }
                    }
                    .flatMap {
                        urls = it
                        val mapFlow = AppContext.gson.fromJson<List<String>>(it[DataSourceType.CENSORED.key] ?: "").map {
                            Flowable.combineLatest(Flowable.just<String>(it),
                                    JAVBusService.INSTANCE.get(it).addUserCase(15).onErrorReturnItem(""),
                                    BiFunction<String, String, Pair<String, String>> { t1, t2 -> t1 to t2 })
                        }
                        Flowable.mergeDelayError(mapFlow).filter { it.second.isNotBlank() }
                    }
                    .firstOrError()
                    .doOnError { CacheLoader.acache.remove(C.Cache.ANNOUNCE_URL) }
                    .map {
                        val ds = DataSourceType.values().takeLast(DataSourceType.values().size - 1).toMutableList()
                        Jsoup.parse(it.second).select(".navbar-nav a").forEach {
                            box ->
                            ds.find { box.text() == it.key }?.let {
                                ds.remove(it)

                                urls.put(it.key, box.attr("href").removeSuffix("/"))
                            }
                        }
                        KLog.d("leave : $ds")
                        urls.get(DataSourceType.XYZ.key)?.let {
                            //欧美
                            urls.put(DataSourceType.XYZ_ACTRESSES.key, "$it/${DataSourceType.XYZ_ACTRESSES.key.split("/").last()}")
                            urls.put(DataSourceType.XYZ_GENRE.key, "$it/${DataSourceType.XYZ_GENRE.key.split("/").last()}")
                        }
                        urls.put(DataSourceType.CENSORED.key, it.first)

                        //change xyz
                        urls[DataSourceType.XYZ_ACTRESSES.key] =  urls[DataSourceType.XYZ_ACTRESSES.key]?.replace("org" ,"xyz")
                        urls[DataSourceType.XYZ.key] =  urls[DataSourceType.XYZ.key]?.replace("org" ,"xyz")
                        urls[DataSourceType.XYZ_GENRE.key] =  urls[DataSourceType.XYZ_GENRE.key]?.replace("org" ,"xyz")

                        KLog.i("urls : ${it.first} , all urls : $urls , at last $ds")

                        CacheLoader.cacheLruAndDisk(C.Cache.BUS_URLS to urls, C.Cache.DAY * 2) //缓存所有的urls
                        KLog.i("get fast it : $it")
                        CacheLoader.lru.put(DataSourceType.CENSORED.key + "false", it.second) //默认有种的
                        urls
                    }.toFlowable()
            return Flowable.concat<ArrayMap<String, String>>(urlsFromDisk, urlsFromNet)
                    .firstElement().toObservable()
                    .subscribeOn(Schedulers.io())
        } else CacheLoader.justLru(C.Cache.BUS_URLS).map {
            AppContext.gson.fromJson<ArrayMap<String, String>>(it).apply {
                KLog.i("map urls $this")
            }
        }.toObservable()
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, SplashActivity::class.java))
        }
    }
}
