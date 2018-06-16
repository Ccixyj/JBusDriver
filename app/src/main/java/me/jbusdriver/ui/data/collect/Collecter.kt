package me.jbusdriver.ui.data.collect

import android.os.Environment
import android.os.StatFs
import android.text.TextUtils
import com.google.gson.*
import com.umeng.analytics.MobclickAgent
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.base.*
import me.jbusdriver.common.JBus
import me.jbusdriver.base.RxBus
import me.jbusdriver.db.service.LinkService
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.bean.*
import java.io.File
import java.lang.reflect.Type

interface ICollect<T> {
    val key: String

    @Deprecated("since1.2")
    val dataList: MutableList<T>

    @Deprecated("since1.2")
    fun reload()

    fun addToCollect(data: T): Boolean
    fun has(data: T): Boolean
    fun removeCollect(data: T): Boolean

    fun update(data: T): Boolean
}

@Deprecated("since 1.2", level = DeprecationLevel.WARNING)
abstract class AbsCollectorImpl<T : ILink> : ICollect<T> {

    protected val host: String by lazy { JAVBusService.defaultFastUrl }
    protected val imageHost: String by lazy { JAVBusService.defaultFastUrl }



    @Deprecated("since version 1.1.1")
    protected open val gson: Gson by lazy { GSON }
    @Deprecated("since version 1.1.1")
    private val collectCache by lazy {
        try {
            if (android.os.Environment.MEDIA_MOUNTED != android.os.Environment.getExternalStorageState()) {
                error("sd mount state : ${android.os.Environment.getExternalStorageState()}")
            }

            val pathSuffix = File.separator + "collect" + File.separator
            val dir: String = createDir(Environment.getExternalStorageDirectory().absolutePath + File.separator + JBus.packageName + pathSuffix)
                    ?: createDir(JBus.filesDir.absolutePath + pathSuffix)
                    ?: error("cant not create collect dir in anywhere")

            //可能存在旧的,复制到新的目录下去并删除
            ACache.get(File(dir).apply {
                if (!this.exists() && !this.mkdirs()) {
                    error("can not create file dir in ${this.absolutePath}")
                }
                if (dir.contains(Environment.getExternalStorageDirectory().absolutePath)) {
                    if ((this.list()?.size ?: -1) > 0) return@apply

                    val fileOld = File(JBus.filesDir.absolutePath + pathSuffix)
                    if (fileOld.exists() && (fileOld.list()?.size ?: -1) > 0) {
                        fileOld.copyRecursively(this)
                        fileOld.deleteRecursively()
                    }
                }

            })
        } catch (e: Exception) {
            MobclickAgent.reportError(JBus, e)
            JBus.toast("收藏目录创建失败,请检查app是否有sd卡操作权限")
            null
        }
    }


    private fun getAvailableExternalMemorySize(): Long {
        return if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val path = Environment.getExternalStorageDirectory()//获取SDCard根目录
            val stat = StatFs(path.path)
            val blockSize = stat.blockSize.toLong()
            val availableBlocks = stat.availableBlocks.toLong()
            availableBlocks * blockSize
        } else {
            -1
        }
    }

    @Deprecated("since version 1.1.1")
    abstract fun transform(cacheString: String): MutableList<T>

    abstract fun checkUrls(data: MutableList<T>): MutableList<T>


    @Deprecated("since version 1.1.1")
    abstract val backUpAction: ((File) -> Unit)?

    private fun refreshData(): MutableList<T> {
        KLog.w("refreshData key $key")
        if (getAvailableExternalMemorySize() < MB * 100) {
            JBus.toast("sd卡可用空间不足100M")
        }
        return transferDB() ?: loadFromDb()
    }

    private fun transferDB(): MutableList<T>? {
        return collectCache?.getAsString(key)?.let {
            val res = try {
                transform(it).let { checkUrls(it) }
            } catch (e: Exception) {
                KLog.w("refreshData key $e")
                mutableListOf<T>()
            }
            //迁移到db
            if (res.isNotEmpty()) {
                LinkService.save(res.asReversed())
                backUp(key)//生成备份
            }
            collectCache?.remove(key)
            //返回
            res
        }
    }

    abstract fun loadFromDb(): MutableList<T>


    private fun backUp(key: String) {
        collectCache?.file(key)?.let {
            val bak = File(it.parent, "$key#${key.hashCode()}.BAK")
            try {
                it.copyTo(bak, true)
                // backUpAction?.invoke(bak)
            } catch (e: Exception) {
            }
        }
    }

    override fun addToCollect(data: T): Boolean {
        if (!has(data)) {
            dataList.add(0, data)
            save(data)
            JBus.toast("${data.des}收藏成功")
            return true
        }
        return false
    }

    override val dataList: MutableList<T> by lazy { refreshData() }

    override fun has(data: T): Boolean = dataList.any { it.uniqueKey == data.uniqueKey }

    override fun removeCollect(data: T): Boolean {
        val d = dataList.remove(dataList.find { it.uniqueKey == data.uniqueKey })
        if (d) {
            if (LinkService.remove(data)) JBus.toast("已取消收藏")
            return true
        }
        return false
    }

    private fun save(data: T) {
        Schedulers.io().scheduleDirect {
            LinkService.save(data)
        }
    }

    override fun reload() {
        dataList.clear()
        dataList.addAll(refreshData())
    }

    override fun update(data: T): Boolean {
        return dataList.find { it.uniqueKey == data.uniqueKey }?.let {
            LinkService.update(data)
        } ?: false
    }
}


@Deprecated("since 1.2", level = DeprecationLevel.ERROR)
object MovieCollector : AbsCollectorImpl<Movie>() {
    override val key: String = "Movie_Key"


    override fun transform(cacheString: String) = gson.fromJson<MutableList<Movie>>(cacheString)
            ?: mutableListOf()

    override fun checkUrls(data: MutableList<Movie>): MutableList<Movie> {
        if (host.endsWith(".xyz")) return data
        val detailChange = data.any { it.link.urlHost != host }
        val imageChange = !TextUtils.isEmpty(imageHost) && data.any { it.imageUrl.urlHost != imageHost }

        return if (detailChange || imageChange) {
            val new = data.mapTo(ArrayList(data.size)) {
                if (it.link.urlHost.endsWith(".xyz")) it
                else it.copy(link = if (detailChange) it.link.replace(it.link.urlHost, host) else it.link, imageUrl = if (imageChange) it.imageUrl.replace(it.imageUrl.urlHost, imageHost) else it.imageUrl)
            }
            new
        } else data
    }


    override val backUpAction = { bakFile: File ->
        RxBus.post(CollectErrorEvent(key, "收藏电影数据格式错误,已转存至${bakFile.absolutePath}"))
    }

    override fun loadFromDb() = LinkService.queryMovies().toMutableList()
}

@Deprecated("since 1.2", level = DeprecationLevel.ERROR)
object ActressCollector : AbsCollectorImpl<ActressInfo>() {
    override val key: String = "Actress_Key"


    override fun transform(cacheString: String) = gson.fromJson<MutableList<ActressInfo>>(cacheString)
            ?: mutableListOf()

    override fun checkUrls(data: MutableList<ActressInfo>): MutableList<ActressInfo> {
        if (host.endsWith(".xyz")) return data
        val linkChange = data.any { it.link.urlHost != host }
        val imageChange = !TextUtils.isEmpty(imageHost) && data.any { it.avatar.urlHost != imageHost && !it.avatar.endsWith("nowprinting.gif") }
        return if (linkChange || imageChange) {
            val new = data.mapTo(ArrayList(data.size)) {
                if (it.link.urlHost.endsWith(".xyz")) it //排除xyz
                else it.copy(link = if (linkChange) it.link.replace(it.link.urlHost, host) else it.link, avatar = if (imageChange) it.avatar.replace(it.avatar.urlHost, imageHost) else it.avatar)
            }
            new
        } else data

    }


    override val backUpAction = { bakFile: File ->
        RxBus.post(CollectErrorEvent(key, "收藏演员数据格式错误,已转存至${bakFile.absolutePath}"))
    }

    override fun loadFromDb(): MutableList<ActressInfo> = LinkService.queryActress().toMutableList()
}

/**
 * header genre searchWord
 */
@Deprecated("since 1.2", level = DeprecationLevel.ERROR)
object LinkCollector : AbsCollectorImpl<ILink>() {

    override val key: String = "Link_Key"
    override val gson: Gson  by lazy { GsonBuilder().registerTypeAdapter(ILink::class.java, ILinkAdapter).create() }

    override fun transform(cacheString: String) = gson.fromJson<MutableList<ILink>>(cacheString)
            ?: mutableListOf()

    override fun checkUrls(data: MutableList<ILink>): MutableList<ILink> {
        if (host.endsWith(".xyz")) return data
        val linkChange = data.any { it.link.urlHost != host }
        return if (linkChange) {
            val new = data.mapTo(ArrayList(data.size)) {
                if (it.link.urlHost.endsWith(".xyz")) it //排除xyz
                else {
                    when (it) {
                        is Header -> it.copy(link = it.link.replace(it.link.urlHost, host))
                        is Genre -> it.copy(link = it.link.replace(it.link.urlHost, host))
                        else -> it
                    }
                }
            }
            new
        } else data

    }

    override val backUpAction = { bakFile: File ->
        RxBus.post(CollectErrorEvent(key, "收藏链接数据格式错误,已转存至${bakFile.absolutePath}"))
    }

    override fun loadFromDb(): MutableList<ILink> = LinkService.queryLink().toMutableList()

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

}