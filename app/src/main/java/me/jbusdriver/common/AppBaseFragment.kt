package me.jbusdriver.common

import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.jbusdriver.mvp.presenter.BasePresenter
import me.jbusdriver.mvp.presenter.loader.PresenterFactory
import me.jbusdriver.mvp.presenter.loader.PresenterLoader
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Created by Administrator on 2016/7/21 0021.
 */
abstract class AppBaseFragment<P : BasePresenter<V>, V> : BaseFragment(), LoaderManager.LoaderCallbacks<P>, PresenterFactory<P> {

    /**
     * Do we need to call [.doStart] from the [.onLoadFinished] method.
     * Will be true if SPresenter wasn't loaded when [.onStart] is reached
     */
    private val mNeedToCallStart = AtomicBoolean(false)
    protected var mFirstStart: Boolean = false//Is this the first start of the fragment (after onCreate)
    protected var mViewReCreate = false //rootViewWeakRef 是否重新创建了
    protected var isUserVisible: Boolean = false
    protected var mBasePresenter: P? = null
    protected var rootViewWeakRef: WeakReference<View>? = null
    private var mUniqueLoaderIdentifier: Int = 0//Unique identifier for the loader, persisted across re-creation
    //lazy load tag
    private var isLazyLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFirstStart = savedInstanceState == null || savedInstanceState.getBoolean(C.SavedInstanceState.RECREATION_SAVED_STATE)
        mUniqueLoaderIdentifier = savedInstanceState?.getInt(C.SavedInstanceState.LOADER_ID_SAVED_STATE) ?: AppBaseActivity.sViewCounter.incrementAndGet()

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loaderManager.initLoader(mUniqueLoaderIdentifier, null, this).startLoading()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        KLog.t(TAG).d("onCreateView : ${rootViewWeakRef?.get()}")
        rootViewWeakRef?.get()?.let {
            ((it.parent as? View) as? ViewGroup)?.also {
                it.removeView(rootViewWeakRef?.get())
            }
        } ?: run {
            if (!mFirstStart) mViewReCreate = true
            inflater.inflate(layoutId, container, false)?.let {
                rootViewWeakRef = WeakReference(it)
            }
        }
        KLog.t(TAG).d("onCreateView ok: ${rootViewWeakRef?.get()}")
        if (savedInstanceState != null)
            onRestartInstance(savedInstanceState)
        return rootViewWeakRef?.get()
    }

    protected abstract val layoutId: Int

    protected abstract fun initWidget(rootView: View)

    protected open fun onRestartInstance(bundle: Bundle) {}


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (mFirstStart || mViewReCreate) {
            initWidget(rootViewWeakRef?.get() ?: error("view is no inflated!!"))
        }
    }

    private fun doStart() {
        KLog.t(TAG).d("doStart : mFirstStart :" + mFirstStart, "mUniqueLoaderIdentifier :" + mUniqueLoaderIdentifier, "instance = " + this)
        requireNotNull(mBasePresenter)
        mBasePresenter?.onViewAttached(this as V)
        mBasePresenter?.onStart(mFirstStart)
        if (mFirstStart || mViewReCreate) {
            initData()
        }
        if (!isLazyLoaded && isUserVisible) {
            lazyLoad()
            isLazyLoaded = true
        }
        mFirstStart = false
        mViewReCreate = false
    }

    override fun onStart() {
        super.onStart()
        if (mBasePresenter == null) {
            mNeedToCallStart.set(true)
        } else {
            doStart()
        }
    }

    override fun onResume() {
        super.onResume()
        mBasePresenter?.onResume()
    }

    /**
     *fragment show hide 不走setUserVisibleHint , 走onHiddenChanged方法.
     */
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            isUserVisible = true
            onVisible()
        } else {
            isUserVisible = false
            onInvisible()
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (userVisibleHint) {
            isUserVisible = true
            onVisible()
        } else {
            isUserVisible = false
            onInvisible()
        }
    }

    protected open fun onVisible() {
        KLog.t(TAG).d("onVisible")
        if (!isUserVisible || isLazyLoaded || mBasePresenter == null) {
            //SPresenter 可能没有初始化 ,放入dostart 中执行lazy

        } else {
            lazyLoad()
            isLazyLoaded = true
        }
    }

    protected open fun lazyLoad() {
        if (mBasePresenter is BasePresenter.LazyLoaderPresenter) (mBasePresenter as? BasePresenter.LazyLoaderPresenter)?.lazyLoad()
    }

    protected open fun onInvisible() {}

    protected open fun initData() {}


    override fun onPause() {
        super.onPause()
        mBasePresenter?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mBasePresenter?.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBasePresenter?.onViewDetached()
    }

    override fun onDestroy() {
        super.onDestroy()
        rootViewWeakRef?.clear()
        rootViewWeakRef = null
    }

    override fun onDetach() {
        super.onDetach()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(C.SavedInstanceState.RECREATION_SAVED_STATE, true)
        outState.putInt(C.SavedInstanceState.LOADER_ID_SAVED_STATE, mUniqueLoaderIdentifier)
    }


    override fun onCreateLoader(id: Int, args: Bundle?) =
            PresenterLoader(viewContext, this)

    /**
     * fragment 会回调两次

     * @param loader
     * *
     * @param data
     */
    override fun onLoadFinished(loader: Loader<P>, data: P) {
        //fragment中会赋值两次，可以设置flag。
        KLog.t(TAG).d("onLoadFinished")
        mBasePresenter = data
        if (mNeedToCallStart.compareAndSet(true, false)) {
            doStart()
        }
    }

    override fun onLoaderReset(loader: Loader<P>) {
        mBasePresenter = null
    }

}
