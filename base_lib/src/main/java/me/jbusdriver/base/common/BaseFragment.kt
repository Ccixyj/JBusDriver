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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KLog.t(TAG).d("onCreate")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        KLog.t(TAG).d("onActivityCreated")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        KLog.t(TAG).d("onCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        KLog.t(TAG).d("onStart")
    }

    override fun onResume() {
        super.onResume()
        KLog.t(TAG).d("onResume")
    }

    override fun onPause() {
        super.onPause()
        KLog.t(TAG).d("onPause")
    }

    override fun onStop() {
        super.onStop()
        KLog.t(TAG).d("onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        KLog.t(TAG).d("onDestroyView")
        rxManager.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        KLog.t(TAG).d("onSaveInstanceState $outState")
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        rxManager.clear()
        rxManager.dispose()
        KLog.t(TAG).d("onDestroy")
    }

    //region other
    val viewContext: Context
        get() = activity ?: JBusManager.context
    //endregion
}
