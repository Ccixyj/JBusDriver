package me.jbusdriver.base.common

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.gyf.barlibrary.ImmersionBar
import com.umeng.analytics.MobclickAgent
import io.reactivex.disposables.CompositeDisposable
import me.jbusdriver.base.BuildConfig
import me.jbusdriver.base.KLog

/**
 * Created by Administrator on 2016/8/11 0011.
 * 日志记录及基础方法复用
 */
abstract class BaseActivity : AppCompatActivity() {
    protected val rxManager by lazy { CompositeDisposable() }
    protected val TAG: String by lazy { this::class.java.simpleName }
    private var destroyed = false

    protected val immersionBar by lazy { ImmersionBar.with(this)!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        KLog.t(TAG).d("onCreate $savedInstanceState")
        super.onCreate(savedInstanceState)
        MobclickAgent.setDebugMode(BuildConfig.DEBUG)
        MobclickAgent.openActivityDurationTrack(BuildConfig.DEBUG)
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL)
    }

    override fun onStart() {
        super.onStart()
        KLog.t(TAG).d("onStart")
    }

    override fun onRestart() {
        super.onRestart()
        KLog.t(TAG).d("onRestart")
    }

    override fun onResume() {
        super.onResume()
        MobclickAgent.onResume(this)
        KLog.t(TAG).d("onResume")
    }

    override fun onPostResume() {
        super.onPostResume()
        KLog.t(TAG).d("onPostResume")
    }

    override fun onPause() {
        super.onPause()
        MobclickAgent.onPause(this)
        KLog.t(TAG).d("onPause")
    }

    override fun onStop() {
        super.onStop()
        KLog.t(TAG).d("onStop")
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        KLog.t(TAG).d("onSaveInstanceState $outState")
    }


    override fun onDestroy() {
        super.onDestroy()
        rxManager.clear()
        rxManager.dispose()
        KLog.t(TAG).d("onDestroy")
        immersionBar.destroy()
        destroyed = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    val isDestroyedCompatible: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= 17)
                isDestroyedCompatible17
            else
                destroyed || super.isFinishing()

        }

    private val isDestroyedCompatible17: Boolean
        @TargetApi(17)
        get() = super.isDestroyed()

    val viewContext: Context by lazy { this }
}
