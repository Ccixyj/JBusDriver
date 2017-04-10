package me.jbusdriver.mvp.model

import com.cfzx.mvp.usercase.UserCase
import io.reactivex.Flowable

abstract class AbstractBaseModel<in T, R>(val op: (T) -> Flowable<R>) : BaseModel<T, R> {

    override fun requestFor(t: T): Flowable<R> {
        return object : UserCase<T, R>() {
            override fun interActor(params: T): Flowable<R> {
                return op.invoke(params)
            }
        }.request(t)
    }

}