package me.jbusdriver.base

import io.reactivex.FlowableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by Joker on 2015/8/10.
 */
object SchedulersCompat {
    /**
     * Don't break the chain: use RxJava's compose() operator
     */
    @JvmStatic
    fun <T> computation(): FlowableTransformer<T, T> =
            FlowableTransformer { it.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.single()) }

    @JvmStatic
    fun <T> io(): FlowableTransformer<T, T> =
            FlowableTransformer { it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.single()) }

    @JvmStatic
    fun <T> single(): FlowableTransformer<T, T> =
            FlowableTransformer { it.subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.single()) }

    @JvmStatic
    fun <T> newThread(): FlowableTransformer<T, T> =
            FlowableTransformer { it.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.single()) }

    @JvmStatic
    fun <T> trampoline(): FlowableTransformer<T, T> =
            FlowableTransformer { it.subscribeOn(Schedulers.trampoline()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.single()) }

    @JvmStatic
    fun <T> mainThread(): FlowableTransformer<T, T> =
            FlowableTransformer { it.observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.single()) }
}
