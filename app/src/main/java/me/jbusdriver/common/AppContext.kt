package me.jbusdriver.common

import android.app.Application
import android.content.Context
import android.os.Environment
import android.support.multidex.MultiDex
import com.billy.cc.core.component.CC
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.squareup.leakcanary.LeakCanary
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import com.wlqq.phantom.library.PhantomCore
import com.wlqq.phantom.library.log.ILogReporter
import io.reactivex.plugins.RxJavaPlugins
import me.jbusdriver.BuildConfig
import me.jbusdriver.base.GSON
import me.jbusdriver.base.JBusManager
import me.jbusdriver.base.arrayMapof
import me.jbusdriver.http.JAVBusService
import java.io.File
import java.util.*


lateinit var JBus: AppContext


class AppContext : Application() {

    val JBusServices by lazy { arrayMapof<String, JAVBusService>() }
    private val isDebug by lazy {
        BuildConfig.DEBUG || File(
            Environment.getExternalStorageDirectory().absolutePath + File.separator +
                    packageName
                    + File.separator + "debug"

        ).exists()
    }

    private val phantomHostConfig by lazy {
        PhantomCore.Config()
            .setCheckSignature(!isDebug)
            .setCheckVersion(!BuildConfig.DEBUG)
            .setDebug(isDebug)
            .setLogLevel(if (isDebug) android.util.Log.VERBOSE else android.util.Log.WARN)
            .setLogReporter(LogReporterImpl())
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this);
    }

    override fun onCreate() {
        super.onCreate()
        JBusManager.setContext(this)
        JBus = this

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        //插件系统尽早初始化
        PhantomCore.getInstance().init(this, phantomHostConfig)

        if (isDebug) {
            LeakCanary.install(this)

//            initializeStetho(this) //chrome://inspect/#devices

            val formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(true)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(2)         // (Optional) How many method line to show. Default 2
                .methodOffset(0)        // (Optional) Hides internal method calls up to offset. Default 5
                // .logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
                .tag("old_driver")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build()

            Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
                override fun isLoggable(priority: Int, tag: String?) = isDebug
            })


            CC.enableVerboseLog(isDebug)
            CC.enableDebug(isDebug)
            CC.enableRemoteCC(isDebug)
        }

        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, null)
        UMConfigure.setLogEnabled(isDebug)
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.LEGACY_AUTO)
        MobclickAgent.setCatchUncaughtExceptions(true)

        RxJavaPlugins.setErrorHandler {
            try {
                if (!isDebug) MobclickAgent.reportError(this, it)
            } catch (e: Exception) {
                //ignore  report error
            }
        }


        this.registerActivityLifecycleCallbacks(JBusManager)
    }


    override fun onLowMemory() {
        super.onLowMemory()
        JBusServices.clear()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        JBusServices.clear()
    }


    companion object {

        private class LogReporterImpl : ILogReporter {

            override fun reportException(throwable: Throwable, message: HashMap<String, Any>) {
                // 使用 Bugly 或其它异常监控平台上报 Phantom 内部捕获的异常
                MobclickAgent.reportError(JBus, throwable)
                MobclickAgent.reportError(JBus, GSON.toJson(message))
            }

            override fun reportEvent(eventId: String, label: String, params: HashMap<String, Any>) {
                // 使用 talkingdata 或其它移动统计平台上报 Phantom 内部自定义事件
            }

            override fun reportLog(tag: String, message: String) {
                // 使用 Bugly 或其它异常监控平台上报 Phantom 内部输出的上下文相关日志
            }
        }
    }
}