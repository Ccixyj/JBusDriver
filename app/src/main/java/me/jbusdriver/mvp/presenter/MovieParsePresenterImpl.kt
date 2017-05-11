package me.jbusdriver.mvp.presenter

import com.cfzx.utils.CacheLoader
import io.reactivex.Flowable
import me.jbusdriver.common.KLog
import me.jbusdriver.common.SchedulersCompat
import me.jbusdriver.common.SimpleSubscriber
import me.jbusdriver.mvp.MoviePareseContract
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.IAttr
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.model.BaseModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class MovieParsePresenterImpl(val link: ILink) : BasePresenterImpl<MoviePareseContract.MovieParseView>(), MoviePareseContract.MovieParsePresenter {

    val parseModel: BaseModel<Int, String> = object : BaseModel<Int, String> {
        override fun requestFor(t: Int) = Flowable.fromCallable { Jsoup.connect(link.link).get().toString() }.doOnNext {
            if (t == 1) CacheLoader.lru.put(link.link, it)
        }

        override fun requestFromCache(t: Int): Flowable<String> = Flowable.concat(CacheLoader.fromLruAsync(link.link), requestFor(t))
                .firstOrError().toFlowable()

    }

    override fun onFirstLoad() {
        super.onFirstLoad()
        KLog.d("link :$link")
        parseModel.requestFromCache(1)
                .map { parse(link, Jsoup.parse(it)) }
                .compose(SchedulersCompat.io()).subscribeWith(object : SimpleSubscriber<IAttr>() {
            override fun onStart() {
                super.onStart()
            }

            override fun onComplete() {
                super.onComplete()
            }

            override fun onError(e: Throwable) {
                super.onError(e)
            }

            override fun onNext(t: IAttr) {
                super.onNext(t)
                mView?.showContent(t)
            }
        })

    }

    fun parse(link: ILink, doc: Document): IAttr? {
        return when (link) {
            is ActressInfo -> {
                ActressInfo.parseActressAttrs(doc)
            }
            else -> null
        }
    }
}