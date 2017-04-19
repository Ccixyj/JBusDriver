package me.jbusdriver.mvp.presenter

import com.cfzx.utils.CacheLoader
import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.toSingle
import me.jbusdriver.common.*
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.MovieDetailContract
import me.jbusdriver.mvp.bean.Magnet
import me.jbusdriver.mvp.bean.MovieDetail
import me.jbusdriver.mvp.bean.detailSaveKey
import me.jbusdriver.mvp.model.AbstractBaseModel
import me.jbusdriver.mvp.model.BaseModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class MovieDetailPresenterImpl : BasePresenterImpl<MovieDetailContract.MovieDetailView>(), MovieDetailContract.MovieDetailPresenter {


    val loadFromNet = { s: String ->
        KLog.d("request for : $s")
        mView?.let {
            view->
            JAVBusService.INSTANCE.get(view.movie.detailUrl).map { MovieDetail.parseDetails(Jsoup.parse(it),view.movie.type) }
                    .doOnNext { mView?.movie?.detailSaveKey?.let { key -> CacheLoader.cacheDisk(key to it) } }
        } ?: Flowable.empty()
    }
    val model: BaseModel<String, MovieDetail> = object : AbstractBaseModel<String, MovieDetail>(loadFromNet) {
        override fun requestFromCache(t: String): Flowable<MovieDetail> {
            val disk = mView?.let {
                CacheLoader.acache.getAsString(it.movie.detailSaveKey)?.let {
                    AppContext.gson.fromJson<MovieDetail>(it)
                }
            }?.toSingle()?.toFlowable() ?: Flowable.empty<MovieDetail>()
            return Flowable.concat(disk, requestFor(t)).firstOrError().toFlowable()
        }
    }

    override fun onFirstLoad() {
        super.onFirstLoad()
        loadDetail()
    }

    override fun onRefresh() {
        mView?.movie?.detailSaveKey?.let {
            //删除缓存和magnet缓存
            CacheLoader.acache.remove(it)
            CacheLoader.acache.remove(it + "_magnet")
            //重新加载
            loadDetail()
            //magnet 不要重新加载
        }
    }

    override fun loadDetail() {
        mView?.movie?.detailUrl?.let {
            model.requestFromCache(it).compose(SchedulersCompat.io())
                    .compose(SchedulersCompat.io())
                    .doOnTerminate { mView?.dismissLoading() }
                    .subscribeWith(object : SimpleSubscriber<MovieDetail>() {
                        override fun onStart() {
                            super.onStart()
                            mView?.showLoading()
                        }

                        override fun onNext(t: MovieDetail) {
                            super.onNext(t)
                            mView?.showContent(t)
                        }
                    })
                    .addTo(rxManager)
        }

    }

    override fun loadMagnets(doc: Element) {
        Flowable.just(doc).map {
            MovieDetail.parseMagnets(it)
        }.compose(SchedulersCompat.io())
                .subscribeWith(object : SimpleSubscriber<List<Magnet>>() {
                    override fun onStart() {
                        super.onStart()
                    }

                    override fun onComplete() {
                        super.onComplete()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                    }

                    override fun onNext(t: List<Magnet>) {
                        super.onNext(t)
                        mView?.loadMagnet(t)
                    }
                })
                .addTo(rxManager)
    }
}