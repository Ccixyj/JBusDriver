package me.jbusdriver.base.common

import android.annotation.TargetApi
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import com.afollestad.materialdialogs.MaterialDialog
import me.jbusdriver.base.KLog
import me.jbusdriver.base.inflate
import me.jbusdriver.base.mvp.BaseView
import me.jbusdriver.base.mvp.presenter.BasePresenter
import me.jbusdriver.base.mvp.presenter.loader.PresenterFactory
import me.jbusdriver.base.mvp.presenter.loader.PresenterLoader
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Administrator on 2016/7/21 0021.
 */
abstract class AppBaseActivity<P : BasePresenter<V>, in V : BaseView> : BaseActivity(), LoaderManager.LoaderCallbacks<P>, PresenterFactory<P>, BaseView {
    /**
     * Do we need to call [.doStart] from the [.onLoadFinished] method.
     * Will be true if SPresenter wasn't loaded when [.onStart] is reached
     */
    private val mNeedToCallStart = AtomicBoolean(false)
    private var mFirstStart: Boolean = false//Is this the first start of the activity (after onCreate)
    protected var mBasePresenter: P? = null
    private var mUniqueLoaderIdentifier: Int = 0//Unique identifier for the loader, persisted across re-creation

    private var placeDialogHolder: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  UIHelper.$optimizeBackgroundOverdraw(this);
        mFirstStart = savedInstanceState == null || savedInstanceState.getBoolean(C.SavedInstanceState.RECREATION_SAVED_STATE)
        mUniqueLoaderIdentifier = savedInstanceState?.getInt(C.SavedInstanceState.LOADER_ID_SAVED_STATE) ?: sViewCounter.incrementAndGet()
        setContentView(this.inflate(layoutId))
        supportLoaderManager.initLoader(mUniqueLoaderIdentifier, savedInstanceState, this@AppBaseActivity)
        if (savedInstanceState != null) {
            intent.putExtra(C.SavedInstanceState.LOADER_SAVED_STATES + mUniqueLoaderIdentifier, savedInstanceState)
        }
    }

    override fun onStart() {
        super.onStart()
        if (mBasePresenter == null) {
            mNeedToCallStart.set(true)
        } else {
            doStart()
        }
    }

    protected open fun doStart() {
        KLog.t(TAG).d("doStart", mFirstStart, mUniqueLoaderIdentifier)
        requireNotNull(mBasePresenter)
        mBasePresenter?.onViewAttached(this as V)
        mBasePresenter?.onStart(mFirstStart)
        /**
         * 恢复状态 @see  onSaveInstanceState
         */
        val bundleKey = C.SavedInstanceState.LOADER_SAVED_STATES + mUniqueLoaderIdentifier
        intent.getBundleExtra(bundleKey)?.let {
            restoreState(it)
            intent.removeExtra(bundleKey)
        }
        mFirstStart = false
    }

    override fun onResume() {
        super.onResume()
        mBasePresenter?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mBasePresenter?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mBasePresenter?.onStop()
    }

    override fun onDestroy() {
        mBasePresenter?.onViewDetached()
        super.onDestroy()
        rxManager.dispose()

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putBoolean(C.SavedInstanceState.RECREATION_SAVED_STATE, mFirstStart)
        outState?.putInt(C.SavedInstanceState.LOADER_ID_SAVED_STATE, mUniqueLoaderIdentifier)
        KLog.d("$TAG onSaveInstanceState $outState")
    }

    protected abstract val layoutId: Int

    /**
     * 不需要权限就忽略
     * 触发时机自己处理
     */
    @TargetApi(Build.VERSION_CODES.M)
    protected fun requestPermission() {
    }


    override fun onCreateLoader(id: Int, args: Bundle?) = PresenterLoader(this, this)

    override fun onLoadFinished(loader: Loader<P>, data: P) {
        mBasePresenter = data
        if (mNeedToCallStart.compareAndSet(true, false)) {
            doStart()
        }
    }

    override fun onLoaderReset(loader: Loader<P>) {
        mBasePresenter = null
    }


    override fun showLoading() {
        runOnUiThread {
            if (viewContext is Application) return@runOnUiThread
            placeDialogHolder = MaterialDialog.Builder(viewContext).content("正在加载...").progress(true, 0).show()
        }

    }

    override fun dismissLoading() {
        runOnUiThread {
            placeDialogHolder?.dismiss()
            placeDialogHolder = null
        }
    }

    protected open fun restoreState(bundle: Bundle) {
        KLog.d("$TAG restoreState : $bundle")
        mBasePresenter?.restoreFromState()
    }

    companion object {
        val sViewCounter = AtomicInteger(Integer.MIN_VALUE)
    }


}
