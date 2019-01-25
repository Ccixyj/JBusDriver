package me.jbusdriver.plugin.magnet

import android.content.Context
import android.widget.Toast
import com.wlqq.phantom.communication.PhantomService
import com.wlqq.phantom.communication.RemoteMethod
import me.jbusdriver.plugin.magnet.common.loader.IMagnetLoader

/**
 * kotlin 的 service 必须open
 */
@PhantomService(name = BuildConfig.APPLICATION_ID + "/MagnetService", version = 1)
open class MagnetService {

    @RemoteMethod(name = "pluginToast")
    open fun pluginToast(context: Context): String {
        Toast.makeText(context, "hello from plugin!", Toast.LENGTH_LONG).show()
        return "hello from plugin!"
    }

    @RemoteMethod(name = "getLoader")
    open fun getLoader(name: String): IMagnetLoader? {
        return MagnetLoaders.Loaders.get(name)
    }

    @RemoteMethod(name = "getAllLoaders")
    open fun getAllLoaders(): Map<String,IMagnetLoader> {
        return MagnetLoaders.Loaders
    }
}