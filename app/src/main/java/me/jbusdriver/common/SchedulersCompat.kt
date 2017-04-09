package me.jbusdriver.common

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
    @JvmStatic fun <T> computation(): FlowableTransformer<T, T> {
        return FlowableTransformer { it.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io()) }
    }

    @JvmStatic fun <T> io(): FlowableTransformer<T, T> {
        return FlowableTransformer { it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io()) }
    }

    @JvmStatic fun <T> newThread(): FlowableTransformer<T, T> {
        return FlowableTransformer { it.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io()) }
    }

    @JvmStatic fun <T> trampoline(): FlowableTransformer<T, T> {
        return FlowableTransformer { it.subscribeOn(Schedulers.trampoline()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io()) }
    }

    @JvmStatic fun <T> mainThread(): FlowableTransformer<T, T> {
        return FlowableTransformer { it.observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io()) }
    }
}
