package me.jbusdriver.mvp.presenter

import com.cfzx.utils.CacheLoader
import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import me.jbusdriver.common.KLog
import me.jbusdriver.common.SchedulersCompat
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.GenrePageContract
import me.jbusdriver.mvp.bean.Genre
import me.jbusdriver.mvp.model.AbstractBaseModel
import me.jbusdriver.mvp.model.BaseModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class GenrePagePresenterImpl(val url: String) : BasePresenterImpl<GenrePageContract.GenrePageView>(), GenrePageContract.GenrePagePresenter {


    val model: BaseModel<String, Document> = object : AbstractBaseModel<String, Document>({ url ->
        JAVBusService.INSTANCE.get(url).map { Jsoup.parse(it) }
    }) {
        override fun requestFromCache(t: String): Flowable<Document> {
            return Flowable.concat(CacheLoader.justLru(url).map { Jsoup.parse(it) }, requestFor(t)).firstOrError().toFlowable()
        }
    }


    override fun onFirstLoad() {
        super.onFirstLoad()
        model.requestFromCache(url)
                .map {
                    val generes = it.select(".genre-box")
                    val titles = generes.prev().map { it.text() }
                    val list = generes.map { it.select("a").map { Genre(it.text(), it.attr("href")) } }

                    mView?.let {
                        it.titleValues.clear()
                        it.fragmentValues.clear()
                        it.titleValues.addAll(titles)
                        it.fragmentValues.addAll(list)
                    }
                    Unit
                }.compose(SchedulersCompat.io())
                .subscribeBy(onError = {
                    KLog.d(it)
                }, onComplete = {
                    mView?.showContent(null)
                }).addTo(rxManager)
    }
}