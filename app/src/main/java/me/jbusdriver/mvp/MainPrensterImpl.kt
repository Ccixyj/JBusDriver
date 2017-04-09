package me.jbusdriver.mvp

import android.support.v4.util.ArrayMap
import com.cfzx.utils.CacheLoader
import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.common.C
import me.jbusdriver.common.KLog
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.presenter.BasePresenterImpl
import org.jsoup.Jsoup

class MainPrensterImpl : BasePresenterImpl<MainContract.MainView>(), MainContract.MainPrenster<MainContract.MainView> {

    override fun onFirstLoad() {
        super.onFirstLoad()
        loadFastJAVUrl()
    }

    override fun loadFastJAVUrl() {
        KLog.d("load loadFastJAVUrl")
        val map: ArrayMap<Flowable<String>, String> = ArrayMap(3)
        JAVBusService.INSTANCE.get(JAVBusService.annonceurl)
                .map { Jsoup.parse(it).select("a").map { it.attr("href") } }
                .flatMap {
                    if (it.isEmpty()) Flowable.empty()
                    else {
                        Flowable.merge(it.map { url ->
                            JAVBusService.INSTANCE.get(url).map {
                                CacheLoader.lru.put(C.Cache.Home, it)
                                url
                            }
                        })
                    }
                }.firstOrError()
                .subscribeOn(Schedulers.io())
                .subscribeBy({ KLog.d("get fast it : ${it}") }, { KLog.e("error : $it") })
                .addTo(rxManager)
    }
}