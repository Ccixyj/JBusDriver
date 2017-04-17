package me.jbusdriver.ui.activity

import android.Manifest
import android.os.Bundle
import android.support.v4.util.ArrayMap
import com.cfzx.utils.CacheLoader
import com.google.gson.JsonObject
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Flowable
import io.reactivex.Observable
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
        Observable.combineLatest<Boolean, ArrayMap<String, String>, Boolean>(RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                initUrls(), BiFunction { t1, t2 -> t1 })
                .doOnError { CacheLoader.acache.clear() }
                .retry(3)
                .subscribeBy(onNext = {
                    KLog.i("success")
                    MainActivity.start(this)
                    finish()
                }, onError = {
                    KLog.e(it.message)
                })
                .addTo(rxManager)

    }

    fun initUrls(): Observable<ArrayMap<String, String>> {
        return if (CacheLoader.lru.get(C.Cache.BUS_URLS).isNullOrBlank()) {
            KLog.d("load initUrls")
            //内存在没有地址时 ,先从disk获取缓存的,没有则从网络下载
            val urlsFromDisk = CacheLoader.justDisk(C.Cache.BUS_URLS).map {
                AppContext.gson.fromJson<ArrayMap<String, String>>(it).apply {
                    KLog.i("map urls $this")
                }
            }
            val urlsFromNet = Flowable.concat(CacheLoader.justDisk(C.Cache.ANNOUNCEURL, false), GitHub.INSTANCE.announce().addUserCase()).firstOrError().toFlowable()
                    .map {
                        AppContext.gson.fromJson<JsonObject>(it).get(C.Cache.ANNOUNCEURL)?.asString?.apply {
                            CacheLoader.cacheDisk(C.Cache.ANNOUNCEURL to this, C.Cache.WEEK)
                        } ?: error("url is not valid")
                    }
                    .flatMap {
                        KLog.d("announce url :$it")
                        JAVBusService.INSTANCE.get(it).addUserCase()
                    }
                    .map {
                        source ->
                        arrayMapof<String, String>().apply {
                            put(DataSourceType.CENSORED.key, Jsoup.parse(source).select("a").map { it.attr("href") }.toJsonString())
                        }
                    }
                    .flatMap {
                        urls = it
                        val mapFlow = AppContext.gson.fromJson<List<String>>(it[DataSourceType.CENSORED.key] ?: "").map {
                            Flowable.combineLatest(Flowable.just<String>(it), JAVBusService.INSTANCE.get(it).addUserCase(),
                                    BiFunction<String, String, Pair<String, String>> { t1, t2 -> t1 to t2 })
                        }
                        Flowable.mergeDelayError(mapFlow)
                    }
                    .firstOrError()
                    .map {
                        val ds = DataSourceType.values().takeLast(DataSourceType.values().size - 1).toMutableList()
                        JAVBusService.defaultFastUrl = it.first
                        Jsoup.parse(it.second).select(".navbar-nav a").forEach {
                            box ->
                            ds.find { box.attr("href").endsWith(it.key) }?.let {
                                ds.remove(it)
                                urls.put(it.key, box.attr("href")?.let {
                                    if (it.endsWith("/")) it else it + "/"
                                })
                            }
                        }
                        urls.put(DataSourceType.CENSORED.key, it.first)
                        urls.get(DataSourceType.XYZ.key)?.let {
                            xyzUrl ->
                            ds.map {
                                if (it.key.contains("xyz")) {
                                    urls.put(it.key, xyzUrl.removeSuffix(it.key) + it.key)
                                }
                            }
                        }
                        KLog.i("urls : ${it.first} , all urls : $urls , at last $ds")
                        CacheLoader.cacheLruAndDisk(C.Cache.BUS_URLS to urls, C.Cache.WEEK) //缓存所有的urls
                        KLog.i("get fast it : $it")
                        CacheLoader.lru.put(DataSourceType.CENSORED.key + "false", it.second) //默认有种的
                        urls
                    }.toFlowable()
            return Flowable.concat<ArrayMap<String, String>>(urlsFromDisk, urlsFromNet)
                    .firstElement().toObservable()
                    .subscribeOn(Schedulers.io())
        } else Observable.empty<ArrayMap<String, String>>()
    }

}
