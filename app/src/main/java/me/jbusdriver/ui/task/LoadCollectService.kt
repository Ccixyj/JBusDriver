package me.jbusdriver.ui.task

import android.app.IntentService
import android.content.Context
import android.content.Intent
import com.umeng.analytics.MobclickAgent
import me.jbusdriver.base.GSON
import me.jbusdriver.base.RxBus
import me.jbusdriver.base.fromJson
import me.jbusdriver.base.toast
import me.jbusdriver.db.bean.LinkItem
import me.jbusdriver.db.service.CategoryService
import me.jbusdriver.db.service.LinkService
import me.jbusdriver.mvp.bean.BackUpEvent
import me.jbusdriver.mvp.bean.convertDBItem
import java.io.File

class LoadCollectService : IntentService("LoadCollectService") {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            when (intent.action) {
                ACTION_COLLECT_LOAD -> handleLoadBakUp(File(intent.getStringExtra(ACTION_COLLECT_LOAD)))
                else -> Unit
            }

        }
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
        fun startLoadBackUp(context: Context, file: File) {
            val intent = Intent(context, LoadCollectService::class.java)
            intent.action = ACTION_COLLECT_LOAD
            intent.putExtra(ACTION_COLLECT_LOAD, file.absolutePath)
            context.startService(intent)
        }
    }

}

