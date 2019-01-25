package me.jbusdriver.common

import android.app.Application
import android.os.Environment
import com.billy.cc.core.component.CC
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.squareup.leakcanary.LeakCanary
import com.umeng.analytics.MobclickAgent
import com.wlqq.phantom.library.PhantomCore
import com.wlqq.phantom.library.log.ILogReporter
import io.reactivex.plugins.RxJavaPlugins
import me.jbusdriver.BuildConfig
import me.jbusdriver.base.JBusManager
import me.jbusdriver.base.arrayMapof
import me.jbusdriver.debug.stetho.initializeStetho
import me.jbusdriver.http.JAVBusService
import java.io.File
import java.util.*


lateinit var JBus: AppContext


class AppContext : Application() {

    val JBusServices by lazy { arrayMapof<String, JAVBusService>() }

    private val phantomHostConfig by lazy {
        PhantomCore.Config()
                .setCheckSignature(false)
                .setCheckVersion(false)
                .setDebug(true)
                .setLogLevel(if (true) android.util.Log.VERBOSE else android.util.Log.WARN)
                .setLogReporter(LogReporterImpl())
    }


    override fun onCreate() {
        super.onCreate()
        JBusManager.setContext(this)
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }

        if (true) {
            LeakCanary.install(this)

            initializeStetho(this) //chrome://inspect/#devices

            val formatStrategy = PrettyFormatStrategy.newBuilder()
                    .showThreadInfo(true)  // (Optional) Whether to show thread info or not. Default true
                    .methodCount(2)         // (Optional) How many method line to show. Default 2
                    .methodOffset(0)        // (Optional) Hides internal method calls up to offset. Default 5
                    // .logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
                    .tag("old_driver")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                    .build()

            Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
                override fun isLoggable(priority: Int, tag: String?) = BuildConfig.DEBUG || File(Environment.getExternalStorageDirectory().absolutePath + File.separator +
                        packageName
                        + File.separator + "debug"

                ).exists()
            })


            CC.enableVerboseLog(true)
            CC.enableDebug(true)
            CC.enableRemoteCC(true)
        }

        PhantomCore.getInstance().init(this, phantomHostConfig)
        MobclickAgent.setDebugMode(BuildConfig.DEBUG)

        RxJavaPlugins.setErrorHandler {
            try {
                if (!BuildConfig.DEBUG) MobclickAgent.reportError(this, it)
            } catch (e: Exception) {
                //ignore  report error
            }
        }

        JBus = this
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
//                MobclickAgent.reportError(JBus,throwable)
//                MobclickAgent.reportError(JBus, GSON.toJson(message))
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