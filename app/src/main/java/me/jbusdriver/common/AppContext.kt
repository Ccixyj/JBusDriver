package me.jbusdriver.common

import android.app.Application
import android.content.pm.PackageManager
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.orhanobut.logger.LogLevel
import com.orhanobut.logger.Logger
import com.umeng.analytics.MobclickAgent
import jbusdriver.me.jbusdriver.BuildConfig

/**
 * Created by Administrator on 2017/4/8.
 */
class AppContext : Application() {

    override fun onCreate() {
        super.onCreate()
        instace = this

        Logger.init("old_driver")                 // default PRETTYLOGGER or use just init()
                .methodCount(3)                 // default 2
                .logLevel(if (BuildConfig.DEBUG) LogLevel.FULL else LogLevel.NONE)        // default LogLevel.FULL

        val metaData = this.packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData
        MobclickAgent.startWithConfigure(MobclickAgent.UMAnalyticsConfig(this, metaData.getString("UMENG_APPKEY"),
                metaData.getString("UMENG_CHANNEL"), MobclickAgent.EScenarioType.E_UM_NORMAL))
    }


    companion object{
        @JvmStatic lateinit var instace: AppContext
        @JvmStatic val gson = GsonBuilder().registerTypeAdapter(Int::class.java, JsonDeserializer<Int> { json, _, _ ->
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
}