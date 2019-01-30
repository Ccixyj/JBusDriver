package me.jbusdriver.base.mvp.presenter

import io.reactivex.disposables.CompositeDisposable
import me.jbusdriver.base.KLog
import me.jbusdriver.base.mvp.BaseView
import kotlin.properties.Delegates

/**
 * Created by Administrator on 2016/11/25 0025.
 */
open class BasePresenterImpl<V : BaseView> : BasePresenter<V> {

    @JvmField
    protected var mView: V? = null
    private var isFirstStart: Boolean by Delegates.notNull()
    protected val rxManager by lazy { CompositeDisposable() }
    private val TAG: String by lazy { this.javaClass.simpleName }


    override fun onViewAttached(view: V) {
        KLog.t(TAG).i("$this:onViewAttached $view")
        mView = view
        assert(mView != null)
    }


    override fun onStart(firstStart: Boolean) {
        isFirstStart = firstStart
        if (firstStart && this !is BasePresenter.LazyLoaderPresenter) {
            //如果是LazyLoaderPresenter , 交给LazyLoaderPresenter处理
            onFirstLoad()
            return
        }
    }


    //可以初始化加载数据
    override fun onFirstLoad() {
    }

    override fun onResume() {
    }

    override fun onPause() {
    }

    override fun onStop() {
    }


    override fun onViewDetached() {
        mView?.dismissLoading()
        rxManager.clear()
        mView = null

    }

    override fun onPresenterDestroyed() {
        KLog.t(TAG).i("$this:onPresenterDestroyed")
        rxManager.clear()
        rxManager.dispose()
    }

    override fun restoreFromState() {
        //no op
    }
}