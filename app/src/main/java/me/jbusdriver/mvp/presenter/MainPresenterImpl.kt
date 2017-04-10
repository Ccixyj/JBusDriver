package me.jbusdriver.mvp.presenter

import android.widget.Toast
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import me.jbusdriver.common.AppContext
import me.jbusdriver.common.KLog
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.MainContract
import org.jsoup.Jsoup

class MainPresenterImpl : BasePresenterImpl<MainContract.MainView>(), MainContract.MainPresenter {

    override fun onFirstLoad() {
        super.onFirstLoad()
        loadFastJAVUrl()
    }

    override fun loadFastJAVUrl() {
        KLog.d("load loadFastJAVUrl")
        Toast.makeText(AppContext.instace, "load loadFastJAVUrl", Toast.LENGTH_LONG).show()
        JAVBusService.INSTANCE.get(JAVBusService.annonceurl)
                .map { Jsoup.parse(it).select("a").map { it.attr("href") } }
                .subscribeBy({
                    KLog.d("get utls ok:  $it")
                }, { KLog.e("error : $it") })
/*                .flatMap {
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
                .subscribeBy({ KLog.d("get fast it : $it") }, { KLog.e("error : $it") })*/
                .addTo(rxManager)


    }
}