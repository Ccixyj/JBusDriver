package me.jbusdriver.ui.task

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Environment
import com.google.gson.*
import com.umeng.analytics.MobclickAgent
import me.jbusdriver.base.*
import me.jbusdriver.common.JBus
import me.jbusdriver.db.bean.LinkItem
import me.jbusdriver.db.service.CategoryService
import me.jbusdriver.db.service.LinkService
import me.jbusdriver.mvp.bean.*
import java.io.File
import java.lang.reflect.Type

class CollectService : IntentService("CollectService") {

    private val KYES by lazy { listOf("Movie_Key", "Actress_Key", "Link_Key") }
    private val gson: Gson  by lazy { GsonBuilder().registerTypeAdapter(ILink::class.java, ILinkAdapter).create() }


    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            when (intent.action) {
                ACTION_COLLECT_MIGRATE -> handleMigrate()
                ACTION_COLLECT_LOAD -> handleLoadBakUp(File(intent.getStringExtra(ACTION_COLLECT_LOAD)))
                //  ACTION_COLLECT_BackUP -> handleBakUp(File(intent.getStringExtra(ACTION_COLLECT_BackUP)))
                else -> Unit
            }

        }
    }

    private lateinit var event: BackUpEvent

    private fun handleLoadBakUp(file: File) {
        if (!file.exists()) return
        try {
            val all = GSON.fromJson<List<LinkItem>>(file.readText()) ?: emptyList()
            if (all.isEmpty()) {
                applicationContext.toast("没有要备份的内容！")
                return
            }
            val s = all.size
            event = BackUpEvent(file.absolutePath, s, 1)
            RxBus.post(event)
            val backs = all.asReversed().mapIndexed { index, linkItem ->
                KLog.d("mapIndexed $index $linkItem")
                if (linkItem.categoryId < 0) return@mapIndexed linkItem
                val item: LinkItem
                try {
                    item = if (CategoryService.getById(linkItem.categoryId) == null) {
                        linkItem.getLinkValue().convertDBItem()
                    } else linkItem

                    LinkService.saveOrUpdate(item)
                } catch (e: Exception) {
                    e.printStackTrace()
                    LinkService.saveOrUpdate(linkItem)
                    return@mapIndexed linkItem
                }

                RxBus.post(event.apply {
                    this.total = s
                    this.index = index + 1
                })
                return@mapIndexed item
            }

            applicationContext.toast("恢复备份成功")
            RxBus.post(event.copy(total = s, index = s))
        } catch (e: Exception) {
            e.printStackTrace()
            MobclickAgent.reportError(this@CollectService, e)
            applicationContext.toast("恢复失败,请重新打开app")
            RxBus.post(event.copy(total = 0, index = 0))
        }

    }

    private fun handleMigrate() {
        KLog.d("handleMigrate")
        try {
            val pathSuffix = File.separator + "collect" + File.separator
            var cacheDir = File(Environment.getExternalStorageDirectory().absolutePath + File.separator + JBus.packageName + pathSuffix)
            //迁移可能存在的
            migrate(cacheDir)
            cacheDir = File(JBus.filesDir.absolutePath + pathSuffix)
            //迁移可能存在的
            migrate(cacheDir)
        } catch (e: Exception) {
            KLog.d("error happen : $e")
            MobclickAgent.reportError(this, e)
        }
    }

    private fun migrate(cacheDir: File) {
        if (!cacheDir.exists()) {
            return
        }
        val cache = ACache.get(cacheDir)

        KYES.forEach {
            try {
                val cacheString = cache.getAsString(it)
                val data = if (cacheString != null) {
                    when (it) {
                        "Movie_Key" -> gson.fromJson<MutableList<Movie>>(cacheString)
                        "Actress_Key" -> gson.fromJson<MutableList<ActressInfo>>(cacheString)
                        "Link_Key" -> gson.fromJson<MutableList<ILink>>(cacheString)
                        else -> emptyList()
                    }
                } else emptyList<ILink>()
                KLog.d("need migrate $data")
                LinkService.saveOrUpdate(data.map { it.convertDBItem() }.asReversed())
            } catch (e: Exception) {
                KLog.d("error happen : $e")
                MobclickAgent.reportError(this, e)
            }
        }
        KYES.forEach { cache.remove(it) }
    }


    companion object {

        private const val ACTION_COLLECT_MIGRATE = "me.jbusdriver.ui.task.action.Collect_Migrate"
        private const val ACTION_COLLECT_LOAD = "me.jbusdriver.ui.task.action.LoadBackUp"
        fun startMigrate(context: Context) {
            val intent = Intent(context, CollectService::class.java)
            intent.action = ACTION_COLLECT_MIGRATE
            context.startService(intent)
        }

        fun startLoadBackUp(context: Context, file: File, callBack: ((Int, Int) -> Unit)? = null) {
            val intent = Intent(context, CollectService::class.java)
            intent.action = ACTION_COLLECT_LOAD
            intent.putExtra(ACTION_COLLECT_LOAD, file.absolutePath)
            context.startService(intent)
        }
    }

}


private object ILinkAdapter : JsonSerializer<ILink>, JsonDeserializer<ILink> {
    private const val CLASSNAME = "LINK_CLASS"
    private const val INSTANCE = "LINK_INSTANCE"

    override fun serialize(src: ILink, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        KLog.d("ILinkAdapter  serialize  $typeOfSrc $src ")
        val retValue = JsonObject()
        val className = src.javaClass.name
        retValue.addProperty(CLASSNAME, className)
        val elem = context.serialize(src)
        retValue.add(INSTANCE, elem)
        return retValue
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ILink {
        KLog.d("ILinkAdapter  deserialize $typeOfT $json ")
        val jsonObject = json.asJsonObject
        val className = jsonObject.get(CLASSNAME)?.asString
                ?: throw error("$json cant not find property $CLASSNAME")

        try {
            return context.deserialize(jsonObject.get(INSTANCE), Class.forName(className))
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            throw JsonParseException(e.message)
        }


    }
}
