package me.jbusdriver.base

import io.reactivex.subscribers.DisposableSubscriber
import retrofit2.HttpException

/**
 * Created by Administrator on 2016/7/21 0021.
 */
open class SimpleSubscriber<T> : DisposableSubscriber<T>() {

    private val TAG: String = this.javaClass.name

    override fun onComplete() {
        cancel()
    }

    override fun onError(e: Throwable) {
        e.printStackTrace()
        KLog.t(TAG).e("onError >> code = info : ${e.message}")
        if (e is HttpException) {
            when (e.code()) {
                404 -> toast("没有结果")
            }
        }
        cancel()
    }

    override fun onNext(t: T) {
    }


}
