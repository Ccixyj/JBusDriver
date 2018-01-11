package me.jbusdriver.common

import android.app.Application
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.squareup.leakcanary.LeakCanary
import com.umeng.analytics.MobclickAgent
import jbusdriver.me.jbusdriver.BuildConfig
import me.jbusdriver.http.JAVBusService


/**
 * Created by Administrator on 2017/4/8.
 */
class AppContext : Application() {

    override fun onCreate() {
        super.onCreate()
        instace = this
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
        val formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(true)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(2)         // (Optional) How many method line to show. Default 2
                .methodOffset(0)        // (Optional) Hides internal method calls up to offset. Default 5
                // .logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
                .tag("old_driver")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build()

        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })

        MobclickAgent.setDebugMode(BuildConfig.DEBUG)

    }

    companion object {
        @JvmStatic lateinit var instace: AppContext
        @JvmStatic
        val gson = GsonBuilder().registerTypeAdapter(Int::class.java, JsonDeserializer<Int> { json, _, _ ->
            if (json.isJsonNull || json.asString.isEmpty()) {
                return@JsonDeserializer null
            }
            try {
                return@JsonDeserializer json.asInt
            } catch (e: NumberFormatException) {
                return@JsonDeserializer null
            }
        }).serializeNulls().create()
        val JBusInstances by lazy { arrayMapof<String, JAVBusService>() }
    }
}