package me.jbusdriver.plugin.magnet.app

import android.app.Application
import com.wlqq.phantom.communication.PhantomServiceManager
import me.jbusdriver.plugin.magnet.MagnetService
import me.jbusdriver.plugin.magnet.MangetJavaService

class PluginMagnetApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PhantomServiceManager.registerService(MagnetService())
        PhantomServiceManager.registerService(MangetJavaService())
    }
}