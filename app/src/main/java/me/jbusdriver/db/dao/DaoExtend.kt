package me.jbusdriver.db.dao

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

@Throws(NoSuchElementException::class)
fun <T> ioBlock(timeout: Long = 3000, timeUnit: TimeUnit = TimeUnit.MILLISECONDS, block: () -> T): T =
        Flowable.fromCallable { block() }.timeout(timeout, timeUnit).subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.trampoline()).blockingFirst()


fun <T> TryIgnoreEx(block: () -> T) {
    try {
        block()
    } catch (e: Exception) {
        //ignore ex

    }
}