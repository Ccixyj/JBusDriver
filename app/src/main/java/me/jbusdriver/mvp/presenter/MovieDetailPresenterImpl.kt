package me.jbusdriver.mvp.presenter

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import me.jbusdriver.common.*
import me.jbusdriver.db.bean.History
import me.jbusdriver.db.service.HistoryService
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.http.RecommendService
import me.jbusdriver.mvp.MovieDetailContract
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.mvp.model.AbstractBaseModel
import me.jbusdriver.mvp.model.BaseModel
import me.jbusdriver.mvp.model.RecommendModel
import org.jsoup.Jsoup
import java.util.*

class MovieDetailPresenterImpl(private val fromHistory: Boolean) : BasePresenterImpl<MovieDetailContract.MovieDetailView>(), MovieDetailContract.MovieDetailPresenter {


    private val loadFromNet = { s: String ->
        KLog.d("request for : $s")
        mView?.let { view ->
            JAVBusService.INSTANCE.get(view.movie.link).addUserCase().map { MovieDetail.parseDetails(Jsoup.parse(it)) }
                    .doOnNext { mView?.movie?.detailSaveKey?.let { key -> CacheLoader.cacheDisk(key to it) } }
        } ?: Flowable.empty()
    }
    val model: BaseModel<String, MovieDetail> = object : AbstractBaseModel<String, MovieDetail>(loadFromNet) {
        override fun requestFromCache(t: String): Flowable<MovieDetail> {
            val disk = Flowable.create({ emitter: FlowableEmitter<MovieDetail> ->
                mView?.let { view ->
                    CacheLoader.acache.getAsString(view.movie.detailSaveKey)?.let {
                        val old = GSON.fromJson<MovieDetail>(it)
                        val res = if (old != null && mView?.movie?.link?.endsWith("xyz") == false) {
                            val new = old.checkUrl(JAVBusService.defaultFastUrl)
                            if (old != new) CacheLoader.cacheDisk(view.movie.detailSaveKey to new)
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
        loadDetail()
        mView?.movie?.let {
            if (!fromHistory)
                HistoryService.insert(History(it.DBtype, Date(), it.toJsonString()))

            val likeKey = it.detailSaveKey + "_like"
            Flowable.fromCallable {
                RecommendModel.getLikeCount(likeKey)
            }.map {
                Math.min(it, 3)
            }.subscribe {
                mView?.changeLikeIcon(it)
            }.addTo(rxManager)

        }


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
        mView?.movie?.link?.let {
            KLog.d("detail url :$it  , movie ${mView?.movie}")
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

    override fun likeIt(movie: Movie, reason: String?) {
        val likeKey = movie.detailSaveKey + "_like"
        Flowable.fromCallable {
            RecommendModel.getLikeCount(likeKey)
        }.flatMap { c ->
            if (c > 3) {
                error("一天点赞最多3次")
            }
            val params = arrayMapof(
                    "uid" to RecommendModel.getLikeUID(likeKey),
                    "key" to RecommendBean(name = "${movie.code} ${movie.title}", img = movie.imageUrl.urlPath, url = movie.link.urlPath).toJsonString()
            )
            if (reason.orEmpty().isNotBlank()) {
                params.put("reason", reason)
            }
            RecommendService.INSTANCE.putRecommends(params).map {
                KLog.d("res : $it")
                RecommendModel.save(likeKey)
                return@map Math.min(c + 1, 3)
            }
        }.onErrorReturn {
            it.message?.let {
                AndroidSchedulers.mainThread().scheduleDirect {
                    mView?.viewContext?.toast(it)
                }
            }
            3
        }.compose(SchedulersCompat.io()).subscribeWith(object : SimpleSubscriber<Int>() {
            override fun onNext(t: Int) {
                super.onNext(t)
                mView?.changeLikeIcon(t)
            }
        }).addTo(rxManager)
    }

    override fun restoreFromState() {
        super.restoreFromState()
        loadDetail()
    }

}