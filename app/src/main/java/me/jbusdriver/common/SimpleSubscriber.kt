package me.jbusdriver.common

import io.reactivex.subscribers.DisposableSubscriber
import org.reactivestreams.Subscription

/**
 * Created by Administrator on 2016/7/21 0021.
 */
open class SimpleSubscriber<T> : DisposableSubscriber<T>() {

    private val TAG: String = this.javaClass.name
    var sub : Subscription? = null

   /* override fun onStart() {
        super.onStart()
        KLog.t(TAG).i(": onStart >>")
    }*/
    override fun onComplete() {
        KLog.t(TAG).i("onCompleted >> ")
        sub?.cancel()
    }

    override fun onError(e: Throwable) {
        e.printStackTrace()
        KLog.t(TAG).e( "onError >> code = info : ${e.message}")
    }

    override fun onNext(t: T) {
        KLog.t(TAG).i("t = $t")
    }
}
