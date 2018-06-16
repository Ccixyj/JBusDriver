package me.jbusdriver.base.mvp.model

import io.reactivex.Flowable
import me.jbusdriver.base.addUserCase

abstract class AbstractBaseModel<in P, R>(private val op: (P) -> Flowable<R>) : BaseModel<P, R> {

    override fun requestFor(t: P): Flowable<R> = op.invoke(t).addUserCase()

}