package me.jbusdriver.mvp.presenter

import com.google.gson.JsonObject
import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import me.jbusdriver.base.GSON
import me.jbusdriver.base.KLog
import me.jbusdriver.base.addUserCase
import me.jbusdriver.base.fromJson
import me.jbusdriver.base.common.C
import me.jbusdriver.base.CacheLoader
import me.jbusdriver.base.SchedulersCompat
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
        Flowable.concat<JsonObject>(CacheLoader.justLru(C.Cache.ANNOUNCE_VALUE).map { GSON.fromJson<JsonObject>(it) },
                GitHub.INSTANCE.announce().addUserCase()
                        .map { GSON.fromJson<JsonObject>(it) } //
                        )
                .firstOrError()
                .map {
                    GSON.fromJson(it.get("update"), UpdateBean::class.java) to
                            GSON.fromJson(it.get("notice"), NoticeBean::class.java)
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