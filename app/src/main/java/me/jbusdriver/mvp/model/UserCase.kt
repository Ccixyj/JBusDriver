package com.cfzx.mvp.usercase

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.common.KLog
import java.util.concurrent.TimeUnit

/**
 * Created by Joker on 2015/10/31.
 * 数据请求的统一处理
 */
class UserCase<in P, R>(val requestAction: (P) -> Flowable<R>) {

    @SuppressWarnings("unchecked")
    fun request(params: P): Flowable<R> =
            requestAction(params)
                    .timeout(30L, TimeUnit.SECONDS, Schedulers.io()) //超时
                    .doOnNext { KLog.t("UserCase").d("$params => $it") }
                    .subscribeOn(Schedulers.io())
                    .take(1)

}
