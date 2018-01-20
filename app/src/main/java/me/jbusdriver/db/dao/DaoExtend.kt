package me.jbusdriver.db.dao

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers

@Throws(NoSuchElementException::class)
fun <T> ioBlock(block: () -> T): T = Flowable.fromCallable { block() }.subscribeOn(Schedulers.io()).blockingFirst()


