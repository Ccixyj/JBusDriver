package me.jbusdriver.common

import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor

object RxBus  {
    private val mBus = PublishProcessor.create<Any>().toSerialized()


    fun post(obj: Any) {
        mBus.onNext(obj)
    }

    fun <T> toFlowable(clz: Class<T>): Flowable<T> {
        return mBus.ofType(clz)
    }

    fun unregisterAll() {
        //解除注册
        mBus.onComplete()
    }

    fun hasSubscribers(): Boolean {
        return mBus.hasSubscribers()
    }
}