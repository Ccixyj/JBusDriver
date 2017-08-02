package me.jbusdriver.mvp.presenter

import com.cfzx.utils.CacheLoader
import com.google.gson.JsonObject
import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import me.jbusdriver.common.*
import me.jbusdriver.http.GitHub
import me.jbusdriver.mvp.MainContract
import me.jbusdriver.mvp.bean.UpdateBean


class MainPresenterImpl : BasePresenterImpl<MainContract.MainView>(), MainContract.MainPresenter {
    override fun onFirstLoad() {
        super.onFirstLoad()
        fetchUpdate()
    }

    private fun fetchUpdate() {
        Flowable.concat<UpdateBean>(CacheLoader.justLru(C.Cache.ANNOUNCE_VALUE).map { AppContext.gson.fromJson<UpdateBean>(it) },
                GitHub.INSTANCE.announce().addUserCase()
                        .map { AppContext.gson.fromJson<JsonObject>(it).get("update") }
                        .map {
                            AppContext.gson.fromJson(it, UpdateBean::class.java)
                        })
                .firstOrError()
                .retry(1)
                .toFlowable()
                .compose(SchedulersCompat.io<UpdateBean>())
                .subscribeBy(onNext = {
                    if (mView?.viewContext?.packageInfo?.versionCode ?: -1 < it.versionCode) {
                        mView?.showContent(it)
                    }
                }, onError = {
                    KLog.d("fetchUpdate error ${it.message}")
                })
                .addTo(rxManager)
    }

}