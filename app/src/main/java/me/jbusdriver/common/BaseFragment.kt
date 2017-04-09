package me.jbusdriver.common

import android.content.Context
import android.support.v4.app.Fragment
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by Administrator on 2016/8/11 0011.
 */
open class BaseFragment : Fragment() {
    protected val TAG: String by lazy { this::class.java.simpleName }
    var rxManager = CompositeDisposable()

    //region other
    val viewContext: Context = activity ?: AppContext.instace
    //endregion
}
