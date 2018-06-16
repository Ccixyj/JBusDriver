package me.jbusdriver.base

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.BackpressureStrategy

object RxBus {
    private val mBus = PublishRelay.create<Any>().toSerialized()

    fun post(obj: Any) {
        KLog.d("post event $obj")
        mBus.accept(obj)
    }

    fun <T> toFlowable(clz: Class<T>) = mBus.ofType(clz).toFlowable(BackpressureStrategy.DROP)

    fun hasSubscribers() = mBus.hasObservers()
}