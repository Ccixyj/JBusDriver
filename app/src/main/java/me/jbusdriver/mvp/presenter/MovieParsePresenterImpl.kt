package me.jbusdriver.mvp.presenter

import me.jbusdriver.common.KLog
import me.jbusdriver.mvp.MovieParseContract
import me.jbusdriver.mvp.bean.ILink

class MovieParsePresenterImpl(val link: ILink) : BasePresenterImpl<MovieParseContract.MovieParseView>(), MovieParseContract.MovieParsePresenter {

   /* val parseModel: BaseModel<Int, String> = object : BaseModel<Int, String> {
        override fun requestFor(t: Int) = Flowable.fromCallable { Jsoup.connect(link.link).get().toString() }.doOnNext {
            if (t == 1) CacheLoader.lru.put(link.link, it)
        }

        override fun requestFromCache(t: Int): Flowable<String> = Flowable.concat(CacheLoader.fromLruAsync(link.link), requestFor(t))
                .firstOrError().toFlowable()

    }*/

    override fun onFirstLoad() {
        super.onFirstLoad()
        KLog.d("link :$link")
      /*  parseModel.requestFromCache(1)
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
        })*/

    }

}