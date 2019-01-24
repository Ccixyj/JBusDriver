package me.jbusdriver.base.cc.debug

import android.app.Application
import com.billy.cc.core.component.CC
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.wlqq.phantom.library.PhantomCore
import io.reactivex.plugins.RxJavaPlugins
import me.jbusdriver.base.JBusManager
import me.jbusdriver.base.KLog

/**
 * use only for debug app
 */
abstract class BaseApp : Application() {

    private val phantomCompConfig by lazy {
        PhantomCore.Config()
                .setCheckSignature(false)
                .setCheckVersion(false)
                .setDebug(true)
                .setLogLevel(android.util.Log.VERBOSE)
    }


    override fun onCreate() {
        super.onCreate()


        val formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(true)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(2)         // (Optional) How many method line to show. Default 2
                .methodOffset(0)        // (Optional) Hides internal method calls up to offset. Default 5
                // .logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
                .tag(getTag())   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build()

        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?) = true
        })

        RxJavaPlugins.setErrorHandler {
           KLog.w("error : ${it.message}")
        }

        CC.enableVerboseLog(true)
        CC.enableDebug(true)
        CC.enableRemoteCC(true)

        PhantomCore.getInstance().init(this, phantomCompConfig)
        JBusManager.setContext(this)
        this.registerActivityLifecycleCallbacks(JBusManager)
    }

    abstract fun getTag(): String
}