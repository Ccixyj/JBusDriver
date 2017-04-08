package me.jbusdriver.mvp.model

import io.reactivex.Flowable

class BaseModelImpl<in T, R> : BaseModel<T, R> {

    override fun requestFor(t: T): Flowable<R> {
        return super.requestFor(t)
    }

    override fun requestForCache(t: T): Flowable<R> {
        return Flowable.empty()
    }
}