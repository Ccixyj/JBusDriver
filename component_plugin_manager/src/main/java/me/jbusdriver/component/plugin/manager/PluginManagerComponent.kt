package me.jbusdriver.component.plugin.manager

import com.billy.cc.core.component.CC
import com.billy.cc.core.component.CCResult
import com.billy.cc.core.component.IComponent
import com.google.gson.JsonObject
import me.jbusdriver.base.GSON
import me.jbusdriver.base.KLog
import me.jbusdriver.base.common.C
import me.jbusdriver.component.plugin.manager.plugin.PluginBean
import me.jbusdriver.component.plugin.manager.plugin.Plugins
import java.io.File
import java.nio.channels.FileChannel
import java.security.MessageDigest


class PluginManagerComponent : IComponent {

    val md5 by lazy { MessageDigest.getInstance("MD5") }

    override fun getName() = C.Components.PluginManager

    override fun onCall(cc: CC): Boolean {

        when (val action = cc.actionName) {
            "plugins.init" -> {
                // it.third?.internal?.takeIf { it.isNotEmpty() }?.let { plugins ->
                //
                //             }
                val plugins = GSON.fromJson(cc.getParamItem<JsonObject>("plugins"), Plugins::class.java)
                plugins?.internal?.takeIf { it.isNotEmpty() }?.let { plugins ->
                    val unChecks = plugins.mapNotNull { plugin ->
                        return@mapNotNull try {
                            //set dir
                            val eTag = plugin.eTag.trim()
                            val fileName = "${plugin.tag}-$eTag.apk"
                            val file = File(fileName)

                            if (file.exists()) {
                                md5.reset()
                                val byteBuffer = file.inputStream().channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
                                md5.update(byteBuffer)
                                val hex = md5.digest().joinToString(separator = "") { b ->
                                    Integer.toHexString(b.toInt() and 0xff).padStart(2, '0')
                                }.trim()
                                if (eTag.equals(hex, true)) {
                                    //下载完成了
                                    checkInstall()
                                    return@mapNotNull null
                                }
                                //需要重新下载
                                file.deleteRecursively()
                                plugin
                            }
                            plugin
                        } catch (e: Exception) {
                            plugin
                        }
                    }
                    KLog.i("need install plugins $unChecks")

                    installPlugins(unChecks)
                    CC.sendCCResult(cc.callId, CCResult.success())
                } ?: kotlin.run {
                    CC.sendCCResult(cc.callId, CCResult.error("$action not find plugin for ${cc.params}"))
                }
            }
            else -> {
                KLog.w("not config action for $cc")
            }
        }

        return false
    }

    private fun installPlugins(unChecks: List<PluginBean>) {

    }

    private fun checkInstall() {

    }
}