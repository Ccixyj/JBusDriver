package me.jbusdriver.common

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.squareup.leakcanary.LeakCanary
import com.tencent.tinker.loader.app.TinkerApplication
import com.tencent.tinker.loader.shareutil.ShareConstants
import com.umeng.analytics.MobclickAgent
import io.reactivex.plugins.RxJavaPlugins
import jbusdriver.me.jbusdriver.BuildConfig
import me.jbusdriver.debug.stetho.initializeStetho
import me.jbusdriver.http.JAVBusService
import java.lang.reflect.Modifier.TRANSIENT

lateinit var JBus: AppContext

val GSON by lazy {
    GsonBuilder().excludeFieldsWithModifiers(TRANSIENT).registerTypeAdapter(Int::class.java, JsonDeserializer<Int> { json, _, _ ->
        if (json.isJsonNull || json.asString.isEmpty()) {
            return@JsonDeserializer null
        }
        try {
            return@JsonDeserializer json.asInt
        } catch (e: NumberFormatException) {
            return@JsonDeserializer null
        }
    }).serializeNulls().create()
}

class AppContext : TinkerApplication(ShareConstants.TINKER_ENABLE_ALL, "me.jbusdriver.common.JBusApplicationLike",
        "com.tencent.tinker.loader.TinkerLoader", false) {


    override fun onCreate() {
        super.onCreate()


        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }

        if (BuildConfig.DEBUG) {
            LeakCanary.install(this)

            val formatStrategy = PrettyFormatStrategy.newBuilder()
                    .showThreadInfo(true)  // (Optional) Whether to show thread info or not. Default true
                    .methodCount(2)         // (Optional) How many method line to show. Default 2
                    .methodOffset(0)        // (Optional) Hides internal method calls up to offset. Default 5
                    // .logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
                    .tag("old_driver")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                    .build()

            Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
                override fun isLoggable(priority: Int, tag: String?) = BuildConfig.DEBUG
            })

            initializeStetho(this) //chrome://inspect/#devices
        }

        MobclickAgent.setDebugMode(BuildConfig.DEBUG)

        RxJavaPlugins.setErrorHandler {
            if (!BuildConfig.DEBUG) MobclickAgent.reportError(this, it)
        }
        if (!::JBus.isInitialized) {
            JBus = this
        }
    }


    companion object {

        val JBusInstances by lazy { arrayMapof<String, JAVBusService>() }
    }
}