package me.jbusdriver.ui.holder

import android.content.Context
import io.reactivex.disposables.CompositeDisposable
import java.lang.ref.WeakReference

/**
 * Created by Administrator on 2017/7/8.
 */
open class BaseHolder(context: Context) {
    protected val weakRef by lazy { WeakReference(context) }
    protected val rxManager by lazy { CompositeDisposable() }

    open fun release() {
        rxManager.clear()
        rxManager.dispose()
        weakRef.clear()
    }
}