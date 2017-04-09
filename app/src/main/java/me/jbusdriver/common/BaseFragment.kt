package me.jbusdriver.common

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by Administrator on 2016/8/11 0011.
 */
open class BaseFragment : Fragment() {
    protected val TAG: String by lazy { this::class.java.simpleName }
    var rxManager = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KLog.d("onCreate")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        KLog.d("onActivityCreated")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        KLog.d("onCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        KLog.d("onStart")
    }

    override fun onResume() {
        super.onResume()
        KLog.d("onResume")
    }

    override fun onPause() {
        super.onPause()
        KLog.d("onPause")
    }
    override fun onStop() {
        super.onStop()
        KLog.d("onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        KLog.d("onDestroyView")
    }
    override fun onDestroy() {
        super.onDestroy()
        KLog.d("onDestroy")
    }
    //region other
    val viewContext: Context = activity ?: AppContext.instace
    //endregion
}
