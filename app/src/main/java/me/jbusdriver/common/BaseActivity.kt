package me.jbusdriver.common

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.BuildConfig
import android.support.v7.app.AppCompatActivity
import com.umeng.analytics.MobclickAgent
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by Administrator on 2016/8/11 0011.
 * 日志记录及基础方法复用
 */
open abstract class BaseActivity : AppCompatActivity() {
    var rxManager = CompositeDisposable()
    protected val TAG: String by lazy { this::class.java.simpleName }
    private var destroyed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        KLog.d("onCreate")
        super.onCreate(savedInstanceState)
        MobclickAgent.setDebugMode(jbusdriver.me.jbusdriver.BuildConfig.DEBUG)
        MobclickAgent.openActivityDurationTrack(BuildConfig.DEBUG)
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL)

    }

    override fun onStart() {
        super.onStart()
        KLog.d("onStart")
    }

    override fun onRestart() {
        super.onRestart()
        KLog.d("onRestart")
    }

    override fun onResume() {
        super.onResume()
        KLog.d("onResume")
    }

    override fun onPostResume() {
        super.onPostResume()
        KLog.d("onPostResume")
    }
    override fun onPause() {
        super.onPause()
        KLog.d("onPause")
    }
    override fun onStop() {
        super.onStop()
        KLog.d("onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        KLog.d("onDestroy")
        destroyed = true
    }

    val isDestroyedCompatible: Boolean
        get() {
            if (Build.VERSION.SDK_INT >= 17) {
                return isDestroyedCompatible17
            } else {
                return destroyed || super.isFinishing()
            }
        }

    private val isDestroyedCompatible17: Boolean
        @TargetApi(17)
        get() = super.isDestroyed()

     val viewContext: Context by lazy { this }
}
