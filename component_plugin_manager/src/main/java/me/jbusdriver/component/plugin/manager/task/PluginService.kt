package me.jbusdriver.component.plugin.manager.task

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Environment
import me.jbusdriver.base.JBusManager
import me.jbusdriver.base.toJsonString
import me.jbusdriver.component.plugin.manager.plugin.PluginBean
import java.io.File

class PluginService : IntentService("LoadCollectService") {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            when (intent.action) {
                ACTION_PLUGINS_DOWNLOAD -> handleDownAndInstall(intent.getStringExtra(ACTION_PLUGINS_DOWNLOAD)
                        ?: "")
                else -> Unit
            }

        }
    }

    private fun handleDownAndInstall(s: String) {
//        val plugins = GSON.fromJson<List<PluginBean>>(s).takeIf { it.isNotEmpty() } ?: return
//        KLog.d("plugins : $plugins")
//        Flowable.fromIterable(plugins)
//                .doOnNext { plugin ->
//                    val fileName = "${plugin.tag}-${plugin.eTag}.apk"
//                    val file = File(fileName)
//                    if (file.exists()    ){
//                        return file
//                    }
//                    return GitHub.INSTANCE.downloadPluginAsync(plugin.url).map {
//                        File("")
//                    }
//                }.subscribe()
    }


    companion object {

        private const val ACTION_PLUGINS_DOWNLOAD = "me.jbusdriver.ui.task.action.plugin.download"
        private val DownLoadPluginDir by lazy {
            File(Environment.getExternalStorageDirectory().absolutePath + File.separator + JBusManager.context.packageName + File.separator + "plugins")
        }


        fun startDownAndInstallPlugins(context: Context, plugins: List<PluginBean>) {
            val intent = Intent(context, PluginService::class.java)
            intent.action = ACTION_PLUGINS_DOWNLOAD
            intent.putExtra(ACTION_PLUGINS_DOWNLOAD, plugins.toJsonString())
            context.startService(intent)
        }
    }

}

