package me.jbusdriver.ui.task

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Environment
import com.umeng.analytics.MobclickAgent
import me.jbusdriver.base.*
import me.jbusdriver.db.bean.LinkItem
import me.jbusdriver.db.service.CategoryService
import me.jbusdriver.db.service.LinkService
import me.jbusdriver.mvp.bean.BackUpEvent
import me.jbusdriver.mvp.bean.convertDBItem
import me.jbusdriver.mvp.bean.plugin.PluginBean
import java.io.File

class LoadCollectService : IntentService("LoadCollectService") {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            when (intent.action) {
                ACTION_COLLECT_LOAD -> handleLoadBakUp(File(intent.getStringExtra(ACTION_COLLECT_LOAD)))
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

    private val event: BackUpEvent = BackUpEvent("", 0, 1)

    private fun handleLoadBakUp(file: File) {
        if (!file.exists()) return
        try {
            val all = GSON.fromJson<List<LinkItem>>(file.readText()) ?: emptyList()
            if (all.isEmpty()) {
                toast("没有要备份的内容！")
                return
            }
            val s = all.size
            event.apply {
                path = file.absolutePath
                total = s
                index = 1
            }
            RxBus.post(event)
            all.asReversed().forEachIndexed { index, linkItem ->

                RxBus.post(event.apply {
                    this.total = s
                    this.index = index + 1
                })
                if (linkItem.categoryId < 0) return@forEachIndexed
                val item: LinkItem
                try {
                    item = if (CategoryService.getById(linkItem.categoryId) == null) {
                        linkItem.getLinkValue().convertDBItem()
                    } else linkItem

                    LinkService.saveOrUpdate(item)
                } catch (e: Exception) {
                    e.printStackTrace()
                    MobclickAgent.reportError(this@LoadCollectService, "恢复数据出错: $e")
                    LinkService.saveOrUpdate(linkItem)
                }
            }

            toast("恢复备份成功")
            RxBus.post(event.copy(total = s, index = s))
        } catch (e: Exception) {
            e.printStackTrace()
            MobclickAgent.reportError(this@LoadCollectService, "恢复出错：$e")
            toast("恢复失败,请重新打开app")
            RxBus.post(event.copy(total = 0, index = 0))
        }

    }


    companion object {

        private const val ACTION_COLLECT_LOAD = "me.jbusdriver.ui.task.action.LoadBackUp"
        private const val ACTION_PLUGINS_DOWNLOAD = "me.jbusdriver.ui.task.action.plugin.download"
        private val DownLoadPluginDir by lazy {
            File(Environment.getExternalStorageDirectory().absolutePath + File.separator + JBusManager.context.packageName + File.separator + "plugins")
        }


        fun startLoadBackUp(context: Context, file: File, callBack: ((Int, Int) -> Unit)? = null) {
            val intent = Intent(context, LoadCollectService::class.java)
            intent.action = ACTION_COLLECT_LOAD
            intent.putExtra(ACTION_COLLECT_LOAD, file.absolutePath)
            context.startService(intent)
        }

        fun startDownAndInstallPlugins(context: Context, plugins: List<PluginBean>) {
            val intent = Intent(context, LoadCollectService::class.java)
            intent.action = ACTION_PLUGINS_DOWNLOAD
            intent.putExtra(ACTION_PLUGINS_DOWNLOAD, plugins.toJsonString())
            context.startService(intent)
        }
    }

}

