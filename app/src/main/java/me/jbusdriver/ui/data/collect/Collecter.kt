package me.jbusdriver.ui.data.collect

import android.os.Environment
import android.os.StatFs
import android.text.TextUtils
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.umeng.analytics.MobclickAgent
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.common.*
import me.jbusdriver.db.service.LinkService
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.bean.*
import java.io.File
import java.lang.reflect.Type

/**
 * Created by Administrator on 2017/9/14.
 */
interface ICollect<T> {
    val key: String
    val dataList: MutableList<T>
    fun addToCollect(data: T): Boolean
    fun has(data: T): Boolean
    fun removeCollect(data: T): Boolean
    fun save()
}

abstract class AbsCollectorImpl<T : ILink> : ICollect<T> {
    protected open val gson: Gson by lazy { AppContext.gson }
    protected val host: String by lazy { JAVBusService.defaultFastUrl }
    protected val imageHost: String by lazy { JAVBusService.defaultImageUrlHost }
    protected val collectCache by lazy {
        try {
            if (android.os.Environment.MEDIA_MOUNTED != android.os.Environment.getExternalStorageState()) {
                error("sd mount state : ${android.os.Environment.getExternalStorageState()}")
            }
            val pathSuffix = File.separator + "collect" + File.separator
            val dir: String = createDir(Environment.getExternalStorageDirectory().absolutePath + File.separator + AppContext.instace.packageName + pathSuffix)
                    ?: createDir(AppContext.instace.filesDir.absolutePath + pathSuffix)
                    ?: error("cant not create collect dir in anywhere")

            //可能存在旧的,复制到新的目录下去并删除
            ACache.get(File(dir).apply {
                if (!this.exists() && !this.mkdirs()) {
                    error("can not create file dir in ${this.absolutePath}")
                }
                if (dir.contains(Environment.getExternalStorageDirectory().absolutePath)) {
                    if ((this.list()?.size ?: -1) > 0) return@apply
                    if (getAvailableExternalMemorySize() < MB * 100) {
                        AppContext.instace.toast("sd卡可用空间不足100M")
                    }
                    val fileOld = File(AppContext.instace.filesDir.absolutePath + pathSuffix)
                    if (fileOld.exists() && (fileOld.list()?.size ?: -1) > 0) {
                        fileOld.copyRecursively(this)
                        fileOld.deleteRecursively()
                    }
                }

            })
        } catch (e: Exception) {
            MobclickAgent.reportError(AppContext.instace, e)
            AppContext.instace.toast("收藏目录创建失败,请检查app是否有sd卡操作权限")
            null
        }
    }
    protected val linkService by lazy { LinkService() }

    private fun createDir(collectDir: String): String? {
        File(collectDir.trim()).let {
            try {
                if (!it.exists() && it.mkdirs()) return collectDir
                if (it.exists()) {
                    if (it.isDirectory) {
                        return collectDir
                    } else {
                        it.delete()
                        createDir(collectDir) //recreate
                    }
                }
            } catch (e: Exception) {
                MobclickAgent.reportError(AppContext.instace, e)
            }
        }
        return null
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

    abstract fun transform(cacheString: String): MutableList<T>
    abstract fun checkUrls(data: MutableList<T>): MutableList<T>
    abstract fun loadFromDb(): MutableList<T>

    abstract val backUpAction: ((File) -> Unit)?

    private fun refreshData(): MutableList<T> {
        return collectCache?.getAsString(key)?.let {
            try {
                transform(it).let { checkUrls(it) }.apply {
                    //迁移到db
                    Schedulers.io().scheduleDirect {
                        if (linkService.save(this)) {
                            collectCache?.remove(key)
                        }
                    }
                }
            } catch (e: Exception) {
                KLog.w("refreshData key $e")
                backUp(key)
                mutableListOf<T>()
            }
        } ?: loadFromDb()
    }


    private fun backUp(key: String) {
        collectCache?.file(key)?.let {
            val bak = File(it.parent, "${key.hashCode()}.BAK")
            try {
                it.copyTo(bak, true)
                backUpAction?.invoke(bak)
            } catch (e: Exception) {
            }
        }
    }

    override fun addToCollect(data: T): Boolean {
        if (!has(data)) {
            dataList.add(0, data)
            save()
            AppContext.instace.toast("${data.des}收藏成功")
            return true
        }
        return false
    }

    override val dataList: MutableList<T> by lazy { refreshData() }


    override fun has(data: T): Boolean = dataList.any { it.link.urlPath == data.link.urlPath }

    override fun removeCollect(data: T): Boolean {
        val res = dataList.remove(data) || (if (has(data)) dataList.remove(dataList.find { it.link.urlPath == data.link.urlPath }) else false)
        if (res) save()
        return res
    }

    override fun save() {
        Schedulers.io().scheduleDirect {
            collectCache?.put(key, dataList.toJsonString())
        }
    }
}


object MovieCollector : AbsCollectorImpl<Movie>() {
    override val key: String = "Movie_Key"


    override fun transform(cacheString: String) = gson.fromJson<MutableList<Movie>>(cacheString) ?: mutableListOf()

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

    override fun loadFromDb(): MutableList<Movie> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val backUpAction = { bakFile: File ->
        RxBus.post(CollectErrorEvent(key, "收藏电影数据格式错误,已转存至${bakFile.absolutePath}"))
    }
}


object ActressCollector : AbsCollectorImpl<ActressInfo>() {
    override val key: String = "Actress_Key"


    override fun transform(cacheString: String) = gson.fromJson<MutableList<ActressInfo>>(cacheString) ?: mutableListOf()

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

    override fun loadFromDb(): MutableList<ActressInfo> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val backUpAction = { bakFile: File ->
        RxBus.post(CollectErrorEvent(key, "收藏演员数据格式错误,已转存至${bakFile.absolutePath}"))
    }
}

/**
 * header grene searchWord
 */
object LinkCollector : AbsCollectorImpl<ILink>() {

    override val key: String = "Link_Key"
    override val gson: Gson  by lazy { GsonBuilder().registerTypeAdapter(ILink::class.java, ILinkAdapter).create() }

    override fun has(data: ILink): Boolean {
        if (data is SearchLink)
            return dataList.any { it is SearchLink && it.query == data.query }
        return super.has(data)
    }

    override fun transform(cacheString: String) = gson.fromJson<MutableList<ILink>>(cacheString) ?: mutableListOf()
    override fun loadFromDb(): MutableList<ILink> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

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

    override fun save() {
        Schedulers.io().scheduleDirect {
            collectCache?.put(key, gson.toJson(dataList, object : TypeToken<MutableList<ILink>>() {

            }.type))
        }
    }

    override val backUpAction = { bakFile: File ->
        RxBus.post(CollectErrorEvent(key, "收藏链接数据格式错误,已转存至${bakFile.absolutePath}"))
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
            val className = jsonObject.get(CLASSNAME)?.asString ?: throw error("$json cant not find property $CLASSNAME")

            try {
                return context.deserialize(jsonObject.get(INSTANCE), Class.forName(className))
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                throw JsonParseException(e.message)
            }


        }
    }

}