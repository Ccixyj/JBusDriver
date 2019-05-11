package me.jbusdriver.plugin.magnet

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.wlqq.phantom.communication.PhantomService
import com.wlqq.phantom.communication.RemoteMethod
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup


/**
 * kotlin 的 service 必须open
 */
@PhantomService(name = BuildConfig.APPLICATION_ID + "/MagnetService", version = 1)
open class MagnetService {


    @RemoteMethod(name = "pluginToast")
    open fun pluginToast(context: Context): String {
        val clazz = Jsoup::class.java
        val info =
            "findclass $clazz  from plugin ! translate android x.\r\nver : ${BuildConfig.VERSION_NAME} ,code: ${BuildConfig.VERSION_CODE}" //update to version 2!!!!
        Toast.makeText(context, info, Toast.LENGTH_LONG).show()

        Log.d("MagnetService", clazz.classLoader.toString())
        Log.d("MagnetService", info)
        return info
    }

    @RemoteMethod(name = "getLoader")
    open fun getLoader(name: String): IMagnetLoader? {
        return MagnetLoaders.Loaders.get(name)
    }

    @RemoteMethod(name = "getAllLoaders")
    open fun getAllLoaders(): Map<String, IMagnetLoader> {
        return MagnetLoaders.Loaders
    }

    @RemoteMethod(name = "getMagnets")
    open fun getMagnets(loader: String, key: String, page: Int): String {
        return JSONArray(
            MagnetLoaders.Loaders.get(loader)?.loadMagnets(key, page)
                ?: emptyList<JSONObject>()
        ).toString()

    }

    @RemoteMethod(name = "getLoaderKeys")
    open fun getLoaderKeys(): List<String> {
        return MagnetLoaders.Loaders.keys.toList()
    }

    @RemoteMethod(name = "fetchMagLink")
    open fun fetchMagLink(magnetLoaderKey: String, url: String): String {
        return MagnetLoaders.Loaders.get(magnetLoaderKey)?.fetchMagnetLink(url) ?: ""
    }

    @RemoteMethod(name = "hasNext")
    open fun hasNext(magnetLoaderKey: String): Boolean {
        return MagnetLoaders.Loaders.get(magnetLoaderKey)?.hasNexPage ?: false
    }
}