package me.jbusdriver.component.magnet.ui.config

import me.jbusdriver.base.*
import me.jbusdriver.component.magnet.MagnetPluginHelper


object Configuration {
    //region magnet
    private const val MagnetSourceS: String = "MagnetSourceS"


    fun getConfigKeys() = GSON.fromJson<MutableList<String>>(getSp(MagnetSourceS) ?: "")?.takeIf { it.isNotEmpty() } ?: let {
        val default = MagnetPluginHelper.getLoaderKeys().take(3)
        saveSp(MagnetSourceS, default.toJsonString())
        default.toMutableList()
    }

    fun saveMagnetKeys(keys: List<String>) = saveSp(MagnetSourceS, keys.toJsonString())
//endregion
}