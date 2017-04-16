package me.jbusdriver.mvp.presenter

import com.cfzx.utils.CacheLoader
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.common.KLog
import me.jbusdriver.mvp.MovieDetailContract
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.mvp.model.AbstractBaseModel
import me.jbusdriver.mvp.model.BaseModel
import me.jbusdriver.ui.data.HtmlContentLoader
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class MovieDetailPresenterImpl : BasePresenterImpl<MovieDetailContract.MovieDetailView>(), MovieDetailContract.MovieDetailPresenter {


    val loadFromNet = { s: String ->
        KLog.d("request for : $s")
        mView?.let {
            HtmlContentLoader(it.viewContext, it.movie.detailUrl).html2Flowable().subscribeOn(AndroidSchedulers.mainThread())
                    .doOnNext { CacheLoader.cacheDisk("$s@${mView?.movie?.date}" to it) }
        } ?: Flowable.empty()


    }
    val model: BaseModel<String, String> = object : AbstractBaseModel<String, String>(loadFromNet) {
        override fun requestFromCache(t: String): Flowable<String> {
            return Flowable.concat(CacheLoader.justDisk("$t@${mView?.movie?.date}"), requestFor(t)).firstOrError().toFlowable()
        }
    }

    override fun onFirstLoad() {
        super.onFirstLoad()
        KLog.d("movie : ${mView?.movie}")
        //  loadDetail()
        mView?.movie?.detailUrl?.let {
            Observable.fromCallable { Jsoup.connect(it).get() }
                    .map { parseDetails(it) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(onNext = { mView?.showContent(it) })
        }
    }

    override fun onRefresh() {
        mView?.movie?.detailUrl?.let {
            CacheLoader.removeCacheLike(it, isRegex = true)
            loadDetail()
        }
    }

    override fun loadDetail() {
        mView?.movie?.detailUrl?.let {
            model.requestFromCache(it).observeOn(AndroidSchedulers.mainThread()).subscribeBy(onNext = { mView?.showContent(it) }, onError = { KLog.d(it) }).addTo(rxManager)
        }

    }

    fun parseDetails(doc: Document): MovieDetail {
        val title = doc.select(".container h3").text()
        val roeMovie = doc.select("[class=row movie]")
        val cover = roeMovie.select(".bigImage").attr("href")
        val headers = mutableListOf<Header>()
        val headersContainer = roeMovie.select(".info")

        headersContainer.select("p[class!=star-show]:has(span:not([class=genre])):not(:has(a))")
                .mapTo(headers) {
                    val split = it.text().split(":")
                    Header(split.first(), split.getOrNull(1) ?: "", "")
                } //解析普通信息

        headersContainer.select("p[class!=star-show]:has(span:not([class=genre])):has(a)")
                .mapTo(headers) {
                    val split = it.text().split(":")
                    Header(split.first(), split.getOrNull(1) ?: "", it.select("p a").attr("href"))
                }//解析附带跳转信息

        val generes = headersContainer.select(".genre:has(a[href*=genre])").map {
            Genre(it.text(), it.select("a").attr("href"))
        }//解析分类


        val actresses = doc.select("#avatar-waterfall .avatar-box").map {
            ActressInfo(it.text(), it.select("img").attr("src"), it.attr("href"))
        }

        val samples = doc.select("#sample-waterfall .sample-box").map {
            ImageSample(it.select("img").attr("title"), it.select("img").attr("src"), it.attr("href"))
        }

        val relatedMovies = doc.select("#related-waterfall .movie-box").map {
            Movie(it.attr("title"), it.select("img").attr("src"), "", "", it.attr("href"))
        }

        return MovieDetail(title, cover, headers, generes, actresses, samples, relatedMovies)
    }
}