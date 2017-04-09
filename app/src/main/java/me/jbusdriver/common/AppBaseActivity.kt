package me.jbusdriver.common

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import com.cfzx.mvp.view.BaseView
import me.jbusdriver.mvp.presenter.BasePresenter
import me.jbusdriver.mvp.presenter.loader.PresenterFactory
import me.jbusdriver.mvp.presenter.loader.PresenterLoader
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Administrator on 2016/7/21 0021.
 */
abstract class AppBaseActivity<P : BasePresenter<V>, V : BaseView> : BaseActivity(), LoaderManager.LoaderCallbacks<P>, PresenterFactory<P> {
    /**
     * Do we need to call [.doStart] from the [.onLoadFinished] method.
     * Will be true if presenter wasn't loaded when [.onStart] is reached
     */
    private val mNeedToCallStart = AtomicBoolean(false)
    protected var mFirstStart: Boolean = false//Is this the first start of the activity (after onCreate)
    protected var mBasePresenter: P? = null

    private var mUniqueLoaderIdentifier: Int = 0//Unique identifier for the loader, persisted across re-creation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  UIHelper.$optimizeBackgroundOverdraw(this);
        mFirstStart = savedInstanceState == null || savedInstanceState.getBoolean(C.SavedInstanceState.RECREATION_SAVED_STATE)
        mUniqueLoaderIdentifier = savedInstanceState?.getInt(C.SavedInstanceState.LOADER_ID_SAVED_STATE) ?: AppBaseActivity.sViewCounter.incrementAndGet()
        setContentView(layoutInflater.inflate(layoutId, null))
        supportLoaderManager.initLoader(mUniqueLoaderIdentifier, savedInstanceState, this@AppBaseActivity)

    }

    override fun onStart() {
        super.onStart()
        if (mBasePresenter == null) {
            mNeedToCallStart.set(true)
        } else {
            doStart()
        }
    }

    protected fun doStart() {
        KLog.t(TAG).d("doStart", mFirstStart, mUniqueLoaderIdentifier)
        assert(mBasePresenter != null)
        mBasePresenter!!.onViewAttached(this as V)
        mBasePresenter!!.onStart(mFirstStart)
        mFirstStart = false
    }

    override fun onResume() {
        super.onResume()
        if (mBasePresenter != null) mBasePresenter!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (mBasePresenter != null) mBasePresenter!!.onPause()
    }

    override fun onStop() {
        super.onStop()
        if (mBasePresenter != null) {
            mBasePresenter!!.onStop()
        }
    }

    override fun onDestroy() {
        if (mBasePresenter != null) {
            mBasePresenter!!.onViewDetached()
        }
        super.onDestroy()
        rxManager.dispose()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(C.SavedInstanceState.RECREATION_SAVED_STATE, true)
        outState.putInt(C.SavedInstanceState.LOADER_ID_SAVED_STATE, mUniqueLoaderIdentifier)
    }

    protected abstract val layoutId: Int

    /**
     * 不需要权限就忽略
     * 触发时机自己处理
     */
    @TargetApi(Build.VERSION_CODES.M)
    protected fun requestPermission() {
    }


    override fun onCreateLoader(id: Int, args: Bundle?): Loader<P> {
        return PresenterLoader(this, this)
    }

    override fun onLoadFinished(loader: Loader<P>, data: P) {
        mBasePresenter = data
        if (mNeedToCallStart.compareAndSet(true, false)) {
            doStart()
        }
    }

    override fun onLoaderReset(loader: Loader<P>) {
        mBasePresenter = null
    }


    companion object {
        val sViewCounter = AtomicInteger(Integer.MIN_VALUE)
    }


}
