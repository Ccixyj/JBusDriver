package me.jbusdriver.mvp.presenter

import com.cfzx.utils.CacheLoader
import com.google.gson.JsonObject
import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import me.jbusdriver.common.*
import me.jbusdriver.http.GitHub
import me.jbusdriver.mvp.MainContract
import me.jbusdriver.mvp.bean.NoticeBean
import me.jbusdriver.mvp.bean.UpdateBean


class MainPresenterImpl : BasePresenterImpl<MainContract.MainView>(), MainContract.MainPresenter {
    override fun onFirstLoad() {
        super.onFirstLoad()
        fetchUpdate()
    }

    private fun fetchUpdate() {
        Flowable.concat<JsonObject>(CacheLoader.justLru(C.Cache.ANNOUNCE_VALUE).map { AppContext.gson.fromJson<JsonObject>(it) },
                GitHub.INSTANCE.announce().addUserCase()
                        .map { AppContext.gson.fromJson<JsonObject>(it) } //
                        )
                .firstOrError()
                .map {
                    AppContext.gson.fromJson(it.get("update"), UpdateBean::class.java) to
                            AppContext.gson.fromJson(it.get("notice"), NoticeBean::class.java)
                }
                .retry(1)
                .toFlowable()
                .compose(SchedulersCompat.io<Pair<UpdateBean,NoticeBean?>>())
                .subscribeBy(onNext = {
                    mView?.showContent(it)

                }, onError = {
                    KLog.d("fetchUpdate error ${it.message}")
                })
                .addTo(rxManager)
    }

}