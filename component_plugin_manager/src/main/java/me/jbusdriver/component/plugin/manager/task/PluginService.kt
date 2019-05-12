package me.jbusdriver.component.plugin.manager.task

import android.app.IntentService
import android.content.Context
import android.content.Intent
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.base.GSON
import me.jbusdriver.base.KLog
import me.jbusdriver.base.fromJson
import me.jbusdriver.library.http.OnProgressListener
import me.jbusdriver.library.http.addProgressListener
import me.jbusdriver.library.http.removeProgressListener
import me.jbusdriver.base.toJsonString
import me.jbusdriver.common.bean.plugin.PluginBean
import me.jbusdriver.component.plugin.manager.PluginManagerComponent
import okio.Okio
import java.io.File

class PluginService : IntentService("PluginService") {


    private val service by lazy { DownloadService.createService() }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            when (intent.action) {
                ACTION_PLUGINS_DOWNLOAD -> {
                    val plugins =
                        GSON.fromJson<List<PluginBean>>(intent.getStringExtra(ACTION_PLUGINS_DOWNLOAD).orEmpty())
                    if (plugins.isNotEmpty()) {
                        handleDownAndInstall(plugins)
                    }
                }
                else -> Unit
            }

        }
    }

    private fun handleDownAndInstall(plugins: List<PluginBean>) {
        KLog.d("handleDownAndInstall ----> $plugins")
        val pnl = object : OnProgressListener {
            override fun onProgress(
                url: String,
                bytesRead: Long,
                totalBytes: Long,
                isDone: Boolean,
                exception: Exception?
            ) {
//                KLog.d("download $url , $bytesRead $totalBytes $isDone")
            }
        }
        addProgressListener(pnl)

        Flowable.fromIterable(plugins)
            .parallel()
            .runOn(Schedulers.io())//指定在哪些线程上并发执行
            .flatMap { pluginBean ->
                val f: File = PluginManagerComponent.getPluginDownloadFile(pluginBean)
                try {
                    f.createNewFile()
                } catch (e: Exception) {
                    f.delete()
                    throw e
                }
                return@flatMap service.downloadPluginAsync(pluginBean.url).map { body ->
                    runCatching {
                        //                        f.outputStream().use {
//                            body.byteStream().copyTo(f.outputStream())
//                            body.close()
//                        }

                        Okio.buffer(Okio.sink(f)).use {
                            it.writeAll(body.source())
                        }

                        f
                    }.onSuccess {
                        PluginManagerComponent.checkInstall(plugin = pluginBean, pluginFile = it)
                    }.onFailure {
                        f.delete()
                    }
                }.toFlowable()
            }.sequentialDelayError().blockingSubscribe({
                KLog.d("download end $it")
                removeProgressListener(pnl)
            }, {
                KLog.d("download error $it")
                removeProgressListener(pnl)
            })


    }

    companion object {

        private const val ACTION_PLUGINS_DOWNLOAD = "me.jbusdriver.ui.task.action.plugin.download"

        fun startDownAndInstallPlugins(context: Context, plugins: List<PluginBean>) {
            val intent = Intent(context, PluginService::class.java)
            intent.action = ACTION_PLUGINS_DOWNLOAD
            intent.putExtra(ACTION_PLUGINS_DOWNLOAD, plugins.toJsonString())
            context.startService(intent)
        }
    }

}

