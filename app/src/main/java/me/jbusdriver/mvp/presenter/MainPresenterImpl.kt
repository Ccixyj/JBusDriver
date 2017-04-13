package me.jbusdriver.mvp.presenter

import com.cfzx.utils.CacheLoader
import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.common.C
import me.jbusdriver.common.KLog
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.http.JAVBusService.Companion.fastUrl
import me.jbusdriver.mvp.MainContract
import org.jsoup.Jsoup

class MainPresenterImpl : BasePresenterImpl<MainContract.MainView>(), MainContract.MainPresenter {

    val urls = mutableListOf<String>()


    override fun onFirstLoad() {
        super.onFirstLoad()
        loadFastJAVUrl()
    }

    override fun loadFastJAVUrl() {
        if (CacheLoader.lru.get(C.Cache.BUS_URLS).isNullOrBlank() && isFirstStart) {
            KLog.d("load loadFastJAVUrl")
            //内存在没有地址时
            JAVBusService.INSTANCE.get(JAVBusService.annonceurl)
                    .map { Jsoup.parse(it).select("a").map { it.attr("href") } }

                    /* .doOnNext {
                         CacheLoader.cacheLruAndDisk(C.Cache.BUS_URLS to it)
                         JAVBusService.INSTANCE = JAVBusService.createService(it.first())
                     }*/
                    .flatMap {
                        if (it.isEmpty()) Flowable.empty()
                        else {
                            urls.addAll(it)
                            Flowable.merge(it.map { url ->
                                JAVBusService.INSTANCE.get(url).map {
                                    CacheLoader.lru.put(C.Cache.Home, it)
                                    url
                                }
                            })
                        }
                    }.firstOrError()
                    .doAfterSuccess {
                        fastUrl = it
                        KLog.d("urls : $urls")
                        CacheLoader.cacheLruAndDisk(C.Cache.BUS_URLS to urls)
                    }
                    .subscribeOn(Schedulers.io())
                    .subscribeBy({ KLog.d("get fast it : $it") }, { KLog.e("error : $it") })
                    .addTo(rxManager)

        }
    }
}