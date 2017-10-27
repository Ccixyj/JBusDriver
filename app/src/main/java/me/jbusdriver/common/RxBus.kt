package me.jbusdriver.common

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

object RxBus {
    private val mBus = PublishRelay.create<Any>().toSerialized()

    fun post(obj: Any) {
        KLog.d("post event $obj")
        mBus.accept(obj)
    }

    fun <T> toFlowable(clz: Class<T>): Flowable<T> {
        return mBus.ofType(clz).toFlowable(BackpressureStrategy.DROP)
    }

    fun hasSubscribers(): Boolean {
        return mBus.hasObservers()
    }
}