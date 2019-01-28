package me.jbusdriver.mvp.presenter

import android.app.Activity
import com.billy.cc.core.component.CC
import com.google.gson.JsonObject
import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import me.jbusdriver.base.*
import me.jbusdriver.base.common.C
import me.jbusdriver.base.mvp.presenter.BasePresenterImpl
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
                    Triple(GSON.fromJson(it.get("update"), UpdateBean::class.java),
                            GSON.fromJson(it.get("notice"), NoticeBean::class.java),
                            it.getAsJsonObject("plugins") ?: JsonObject())
                }
                .retry(1)
                .toFlowable()
                .compose(SchedulersCompat.io<Triple<UpdateBean, NoticeBean?, JsonObject>>())
                .subscribeBy(onNext = {
                    mView?.showContent(it.first)
                    mView?.showContent(it.second)
                    if (it.third.size() > 0) {
                        mView?.viewContext?.let { ctx ->
                            //检查内部plugin是否需要更新级初始化
                            CC.obtainBuilder(C.Components.PluginManager)
                                    .setActionName("plugins.init")
                                    .addParam("plugins", it.third)
                                    .cancelOnDestroyWith(ctx as? Activity)
                                    .build()
                                    .callAsync()
                        }

                    }
                }, onError = {
                    KLog.w("fetchUpdate error ${it.message}")
                })
                .addTo(rxManager)
    }

}