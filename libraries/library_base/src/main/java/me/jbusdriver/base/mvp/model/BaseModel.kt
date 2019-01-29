package me.jbusdriver.base.mvp.model

import io.reactivex.Flowable

/**
 * Created by Administrator on 2017/4/8.
 */
interface BaseModel<in T, R> {
    //默认请求q
    fun requestFor(t: T): Flowable<R> = Flowable.empty()
    fun requestFromCache(t: T): Flowable<R> //默认请求

}