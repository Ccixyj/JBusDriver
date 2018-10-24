package me.jbusdriver.mvp.presenter

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.rxkotlin.addTo
import me.jbusdriver.base.*
import me.jbusdriver.base.mvp.model.AbstractBaseModel
import me.jbusdriver.base.mvp.model.BaseModel
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.MovieDetailContract
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.bean.MovieDetail
import me.jbusdriver.mvp.bean.checkUrl
import org.jsoup.Jsoup

class MovieDetailPresenterImpl(private val fromHistory: Boolean) : BasePresenterImpl<MovieDetailContract.MovieDetailView>(), MovieDetailContract.MovieDetailPresenter {


    private val loadFromNet = { s: String ->
        KLog.d("request for : $s")
        JAVBusService.INSTANCE.get(s).addUserCase().map { MovieDetail.parseDetails(Jsoup.parse(it)) }
                .doOnNext { s.urlPath.let { key -> CacheLoader.cacheDisk(key to it) } }
                ?: Flowable.empty()
    }
    val model: BaseModel<String, MovieDetail> = object : AbstractBaseModel<String, MovieDetail>(loadFromNet) {
        override fun requestFromCache(t: String): Flowable<MovieDetail> {
            val disk = Flowable.create({ emitter: FlowableEmitter<MovieDetail> ->
                mView?.let { view ->
                    val saveKey = t.urlPath
                    CacheLoader.acache.getAsString(saveKey)?.let {
                        val old = GSON.fromJson<MovieDetail>(it)
                        val res = if (old != null && mView?.movie?.link?.endsWith("xyz") == false) {
                            val new = old.checkUrl(JAVBusService.defaultFastUrl)
                            if (old != new) CacheLoader.cacheDisk(saveKey to new)
                            new
                        } else old
                        emitter.onNext(res)
                    } ?: emitter.onComplete()
                } ?: emitter.onComplete()
            }, BackpressureStrategy.DROP)

            return Flowable.concat(disk, requestFor(t)).firstOrError().toFlowable()
        }
    }


    override fun onFirstLoad() {
        super.onFirstLoad()
        val fromUrl = mView?.movie?.link ?: mView?.url ?: error("need url info")
        loadDetail(fromUrl)
    }

    override fun onRefresh() {
        mView?.movie?.link?.let {
            //删除缓存和magnet缓存
            CacheLoader.acache.remove(it.urlPath)
            CacheLoader.acache.remove(it.urlPath + "_magnet")
            //重新加载
            loadDetail(it)
            //magnet 不要重新加载
        }
    }

    override fun loadDetail(url: String) {
        model.requestFromCache(url).compose(SchedulersCompat.io())
                .compose(SchedulersCompat.io())
                .doOnTerminate { mView?.dismissLoading() }
                .subscribeWith(object : SimpleSubscriber<MovieDetail>() {
                    override fun onStart() {
                        super.onStart()
                        mView?.showLoading()
                    }

                    override fun onNext(t: MovieDetail) {
                        super.onNext(t)
                        mView?.showContent(t.generateMovie(url))
                        mView?.showContent(t)
                    }
                })
                .addTo(rxManager)

    }

    fun MovieDetail.generateMovie(url: String): Movie {
        val code = headers.first().value.trim()
        return Movie(title.replace(code, "", true).trim(), this.cover.replace("cover", "thumb").replace("_b", ""),
                code, headers.component2().value, url)
    }


    override fun restoreFromState() {
        super.restoreFromState()
        mView?.movie?.link?.let {
            loadDetail(it)
        }
    }

}