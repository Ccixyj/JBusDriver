package me.jbusdriver.mvp.presenter

import android.support.v4.util.ArrayMap
import com.cfzx.utils.CacheLoader
import com.google.gson.JsonObject
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.common.*
import me.jbusdriver.http.GitHub
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.http.JAVBusService.Companion.defaultFastUrl
import me.jbusdriver.mvp.MainContract
import me.jbusdriver.ui.data.DataSourceType
import org.jsoup.Jsoup

class MainPresenterImpl : BasePresenterImpl<MainContract.MainView>(), MainContract.MainPresenter {

    var urls = arrayMapof<String, String>()

    override fun onFirstLoad() {
        super.onFirstLoad()
        initUrls()
    }


    override fun initUrls() {
        if (CacheLoader.lru.get(C.Cache.BUS_URLS).isNullOrBlank() && isFirstStart) {
            KLog.d("load initUrls")
            //内存在没有地址时 ,先从disk获取缓存的,没有则从网络下载
            val urlsFromDisk = CacheLoader.justDisk(C.Cache.BUS_URLS).map { AppContext.gson.fromJson<ArrayMap<String, String>>(it) }
            val urlsFromNet = Flowable.concat(CacheLoader.justDisk(C.Cache.ANNOUNCEURL, false), GitHub.INSTANCE.announce().addUserCase()).firstOrError().toFlowable()
                    .map {
                        AppContext.gson.fromJson<JsonObject>(it).get(C.Cache.ANNOUNCEURL)?.asString?.apply {
                            CacheLoader.cacheDisk(C.Cache.ANNOUNCEURL to this, ACache.TIME_DAY)
                        } ?: error("url is not valid")
                    }
                    .flatMap {
                        JAVBusService.INSTANCE.get(it).addUserCase()
                    }
                    .map {
                        source ->
                        arrayMapof<String, String>().apply {
                            put(DataSourceType.CENSORED.key, Jsoup.parse(source).select("a").map { it.attr("href") }.toJsonString())
                            /*   put(C.Key.UNCENSORED, Jsoup.parse(source).select("a").map { it.attr("href") })
                               put(C.Key.GENRE, Jsoup.parse(source).select("a").map { it.attr("href") })
                               put(C.Key.UNCENSORED_GENRE, Jsoup.parse(source).select("a").map { it.attr("href") })
                               put(C.Key.ACTRESSES, Jsoup.parse(source).select("a").map { it.attr("href") })
                               put(C.Key.UNCENSORED_ACTRESSES, Jsoup.parse(source).select("a").map { it.attr("href") })
                               put(C.Key.XYZ, Jsoup.parse(source).select("a").map { it.attr("href") })
                               put(C.Key.GENRE_HD, Jsoup.parse(source).select("a").map { it.attr("href") })*/
                        }
                    }
            Flowable.concat(urlsFromDisk, urlsFromNet)
                    .firstElement().toFlowable()
                    .flatMap {
                        urls = it
                        val mapFlow = AppContext.gson.fromJson<List<String>>(it[DataSourceType.CENSORED.key] ?: "").map {
                            Flowable.combineLatest(Flowable.just<String>(it), JAVBusService.INSTANCE.get(it).addUserCase(),
                                    BiFunction<String, String, Pair<String, String>> { t1, t2 -> t1 to t2 })
                        }
                        Flowable.mergeDelayError(mapFlow)
                    }
                    .firstOrError()
                    .doAfterSuccess {
                        val ds = DataSourceType.values().takeLast(DataSourceType.values().size - 1).toMutableList()
                        defaultFastUrl = it.first
                        Jsoup.parse(it.second).select(".overlay a ").forEach {
                            box ->
                            ds.find { box.attr("href").endsWith(it.key) }?.let {
                                ds.remove(it)
                                KLog.i("initUrls find $it , ${box.attr("href")}")
                                urls.put(it.key, box.attr("href"))
                            }
                        }
                        KLog.d("urls : ${it.first} , all urls : $urls")
                        CacheLoader.cacheLruAndDisk(C.Cache.BUS_URLS to urls, ACache.TIME_DAY)
                    }
                    .subscribeOn(Schedulers.io())
                    .retry(3)
                    .subscribeBy({
                        KLog.d("get fast it : $it")
                        //把数据放入内存
                        CacheLoader.lru.put(C.Cache.Home, it.second)
                    }, {
                        KLog.e("error : $it")
                        //如果失败尝试清空缓存,这样将直接从网络获取
                        CacheLoader.acache.clear()
                    })
                    .addTo(rxManager)

        }
    }
}