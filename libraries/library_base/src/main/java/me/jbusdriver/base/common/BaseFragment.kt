package me.jbusdriver.base.common

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.CompositeDisposable
import me.jbusdriver.base.JBusManager
import me.jbusdriver.base.KLog

/**
 * Created by Administrator on 2016/8/11 0011.
 */
open class BaseFragment : Fragment() {
    protected val TAG: String by lazy { this::class.java.simpleName }
    val rxManager by lazy { CompositeDisposable() }
    protected val tempSaveBundle by lazy { Bundle() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        KLog.t(TAG).d("onCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        KLog.t(TAG).d("onDestroyView")
        rxManager.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        rxManager.clear()
        rxManager.dispose()
    }

    //region other
    val viewContext: Context
        get() = activity ?: JBusManager.context
    //endregion
}
