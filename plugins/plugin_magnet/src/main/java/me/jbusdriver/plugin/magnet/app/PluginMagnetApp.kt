package me.jbusdriver.plugin.magnet.app

import android.app.Application
import android.util.Log
import com.wlqq.phantom.communication.PhantomServiceManager
import me.jbusdriver.plugin.magnet.MagnetService

public lateinit var instance :Application

class PluginMagnetApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.i("PluginMagnetApp", "onCreate: app created!")
        PhantomServiceManager.registerService(MagnetService())
    }
}