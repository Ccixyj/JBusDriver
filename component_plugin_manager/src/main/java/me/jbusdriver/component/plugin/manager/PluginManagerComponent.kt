package me.jbusdriver.component.plugin.manager

import android.content.Context
import android.os.Environment
import com.billy.cc.core.component.CC
import com.billy.cc.core.component.CCResult
import com.billy.cc.core.component.IComponent
import com.google.gson.JsonObject
import me.jbusdriver.base.GSON
import me.jbusdriver.base.IO_Worker
import me.jbusdriver.base.JBusManager
import me.jbusdriver.base.KLog
import me.jbusdriver.base.common.C
import me.jbusdriver.common.bean.plugin.PluginBean
import me.jbusdriver.common.bean.plugin.Plugins
import me.jbusdriver.component.plugin.manager.task.PluginService
import java.io.File
import java.nio.channels.FileChannel
import java.security.MessageDigest


class PluginManagerComponent : IComponent {

    private val MD5 by lazy { MessageDigest.getInstance("MD5") }


    override fun getName() = C.Components.PluginManager

    override fun onCall(cc: CC): Boolean {
        try {
            when (cc.actionName) {
                "plugins.init" -> {
                    // async call
                    val plugins = GSON.fromJson(cc.getParamItem<JsonObject>("plugins"), Plugins::class.java)
                        ?: error("need param plugins ")
                    //后续操作
                    IO_Worker.schedule {
                        initPlugins(cc, plugins)
                    }
                    return true
                }

                "plugins.info" -> {
                    // async call
                    checkPluginsInComps()
                    CC.sendCCResult(cc.callId, CCResult.successWithNoKey(Plugin_Maps.values.flatten()))
                }
                else -> {
                    CC.sendCCResult(cc.callId, CCResult.errorUnsupportedActionName())
                }
            }
        } catch (e: Exception) {
            KLog.w("$cc call error $e")
            CC.sendCCResult(cc.callId, CCResult.error(e.message))
        }

        return false
    }

    private fun initPlugins(cc: CC, plugins: Plugins) {
        checkPluginsInComps()
        if (!pluginsDir.exists()) pluginsDir.mkdirs()
        plugins.internal.takeIf { it.isNotEmpty() }?.let {
            val need = checkPluginNeedUpdate(it)
            validateDownload(cc, need)
        }
        if (!cc.isStopped) {
            CC.sendCCResult(cc.callId, CCResult.success())
        }
    }

    /**
     * plugin not download
     */
    private fun validateDownload(cc: CC, plugins: List<PluginBean>) {
        val downs = mutableListOf<PluginBean>()
        plugins.forEach { plugin ->
            try {
                //set dir
                val file = getPluginDownloadFile(plugin)
                if (file.exists()) {
                    MD5.reset()
                    val byteBuffer = file.inputStream().channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
                    MD5.update(byteBuffer)
                    val hex = MD5.digest().joinToString(separator = "") { b ->
                        Integer.toHexString(b.toInt() and 0xff).padStart(2, '0')
                    }.trim()
                    if (plugin.eTag.trim().equals(hex, true)) {
                        //下载完成了
                        checkInstall(plugin, file)
                    } else {
                        //需要重新下载
                        downs.add(plugin)
                    }
                } else {
                    downs.add(plugin)
                }
            } catch (e: Exception) {
                KLog.w("validateDownload error $e")
            }
        }
        //donwload
        if (downs.isNotEmpty()) {
            downloadPlugins(cc.context, downs)
        }

    }


    private fun downloadPlugins(ctx: Context, unChecks: List<PluginBean>) {
        KLog.i("downloadPlugins $unChecks")
        PluginService.startDownAndInstallPlugins(ctx, unChecks)
    }


    companion object {
        private val Plugin_Maps = mutableMapOf<String, List<PluginBean>>()
        private val pluginsDir by lazy {
            File(
                Environment.getExternalStorageDirectory().absolutePath + File.separator +
                        JBusManager.context.applicationContext.packageName + File.separator + "plugins" + File.separator
            )
        }

        fun getPluginDownloadFile(plugin: PluginBean) = kotlin.run {
            val eTag = plugin.eTag.trim()
            val fileName = "${plugin.name}-${plugin.versionName}-${plugin.versionCode}-$eTag.apk"
            File(pluginsDir, fileName)

        }

        /**
         * 获取支持插件的组件相关信息
         * @see me.jbusdriver.base.common.C.PluginComponents
         */
        private fun checkPluginsInComps() {
            val comps = C.PluginComponents.AllPlugins()
            KLog.d("checkPluginsInComps ${comps.joinToString()}")
            comps.mapNotNull { name ->
                try {
                    KLog.d("checkPluginsInComps $name for plugins.all ...")
                    CC.obtainBuilder(name)
                        .setActionName("plugins.all")
                        .build().call()
                        .getDataItem<List<PluginBean>>("plugins")?.apply {
                            KLog.d("check comp $name for plugins.all has plugins $this")
                            Plugin_Maps[name] = this
                        }
                } catch (e: Exception) {
                    KLog.d("checkPluginsInComps name $name error $e ")
                    null
                }

            }
        }

        /**
         * 检查是否需要更新
         * @param plugins :api返回的plugin 信息
         */
        fun checkPluginNeedUpdate(plugins: List<PluginBean>): List<PluginBean> {
            val allInstalled = Plugin_Maps.values.flatten()
            KLog.d("all installed plugin : $allInstalled")
            return plugins.filter { pl ->
                val installPlugin = allInstalled.find {
                    it.name == pl.name
                } ?: return@filter true
                installPlugin.versionCode < pl.versionCode
            }
        }

        /**
         * call comp install for  plugin file at last
         */
        fun checkInstall(plugin: PluginBean, pluginFile: File) {
            KLog.i("checkInstall $plugin for $pluginFile Plugin_Maps -> $Plugin_Maps")
            val where =
                Plugin_Maps.filter { it.value.find { it.name == plugin.name } != null }.keys.takeIf { it.isNotEmpty() }
                    ?: Plugin_Maps.keys
            where.forEach {
                CC.obtainBuilder(it)
                    .setActionName("plugins.install")
                    .addParam("path", pluginFile.absolutePath)
                    .setTimeout(10)
                    .build().callAsync()
            }
        }


    }
}
