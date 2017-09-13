package me.jbusdriver.ui.data

import android.os.Environment
import android.os.StatFs
import android.text.TextUtils
import com.google.gson.reflect.TypeToken
import com.umeng.analytics.MobclickAgent
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.common.*
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.CollectErrorEvent
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.bean.Movie
import java.io.File


/**
 * Created by Administrator on 2017/7/16.
 */
object CollectManager {

    private val host: String by lazy { JAVBusService.defaultFastUrl }
    private val imageHost: String by lazy { JAVBusService.defaultImageUrlHost }
    private val collectCache by lazy {
        try {
            val pathSuffix = File.separator + "collect" + File.separator
            val dir: String = createDir(Environment.getExternalStorageDirectory().absolutePath + File.separator + AppContext.instace.packageName + pathSuffix)
                    ?: createDir(AppContext.instace.filesDir.absolutePath + pathSuffix)
                    ?: error("cant not create collect dir in anywhere")

            //可能存在旧的,复制到新的目录下去并删除
            ACache.get(File(dir).apply {
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

    private fun createDir(collectDir: String): String? {
        File(collectDir.trim()).let {
            try {
                if (!it.exists()) it.mkdirs()
            } catch (e: Exception) {
                MobclickAgent.reportError(AppContext.instace, e)
            }
        }
        return collectDir
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

    /*===========数据=============*/
    val movieCache by lazy { getCollectData<Movie>() }
    val actressCache by lazy { getCollectData<ActressInfo>() }


    /*===========添加收藏=============*/
    fun addToCollect(actressInfo: ActressInfo) = addTo(actressInfo)

    fun addToCollect(movie: Movie): Boolean = addTo(movie)

    /*===========是否收藏了=============*/
    fun has(act: ActressInfo) = getCollectData<ActressInfo>().has(act)

    fun has(movie: Movie): Boolean = getCollectData<ActressInfo>().has(movie)

    /*===========删除收藏=============*/
    fun removeCollect(act: ActressInfo) = remove(act)

    fun removeCollect(movie: Movie): Boolean = remove(movie)

    /* ======== save  =========== */
    fun saveActress() = save<ActressInfo>()

    fun saveMovie() = save<Movie>()


    /*泛型*/


    private val _CollectHolder by lazy { HashMap<String, MutableList<*>>() }

    private inline fun <reified T : ILink> getCollectData(): MutableList<T> = _CollectHolder.getOrPut(T::class.java.name) {
        refreshData<T>()
    } as MutableList<T>

    private inline fun <reified T : ILink> addTo(data: T): Boolean {
        val dataList = getCollectData<T>()
        return if (dataList.has(data)) {
            val first = dataList.first()
            when (first) {
                is ActressInfo -> {
                    AppContext.instace.toast("${first.name}已收藏")
                    first.tag = null
                    return false
                }
                is Movie -> {
                    data as Movie
                    AppContext.instace.toast("${first.title}已收藏")
                    return false
                }
            }
            false
        } else {
            getCollectData<T>().add(0, data)
            save<T>()
            true
        }


    }

    private fun <T : ILink> List<T>.has(data: T): Boolean = this.any {
        it.link.urlPath == data.link.urlPath
    }

    private inline fun <reified T : ILink> remove(data: T): Boolean = getCollectData<T>().let {
        val res = it.remove(data) || (if (it.has(data)) it.remove(data) else false)
        if (res) save<T>()
        return res
    }


    private inline fun <reified T : ILink> refreshData(): MutableList<T> {
        val key = T::class.java.name
        return collectCache?.getAsString(T::class.java.name)?.let {
            try {
                AppContext.gson.fromJson<Array<T>>(it, TypeToken.getArray(T::class.java).type).toMutableList()
            } catch (e: Exception) {
                KLog.w("refreshData $e")
                backUp(key)
                mutableListOf<T>()
            }
        } ?: mutableListOf()

    }

    //自定义检查
    private fun <T : ILink> checkUrls(data: MutableList<T>): MutableList<T> {
        if (host.endsWith(".xyz")) return data
        if (data.isEmpty()) return data
        return when (data.first()) {
            is ActressInfo -> {
                data as MutableList<ActressInfo>
                val linkChange = data.any { it.link.urlHost != host }
                val imageChange = !TextUtils.isEmpty(imageHost) && data.any { it.avatar.urlHost != imageHost && !it.avatar.endsWith("nowprinting.gif") }
                return if (linkChange || imageChange) {
                    val new = data.mapTo(ArrayList(data.size)) {
                        if (it.link.urlHost.endsWith(".xyz")) it //排除xyz
                        else it.copy(link = if (linkChange) it.link.replace(it.link.urlHost, host) else it.link, avatar = if (imageChange) it.avatar.replace(it.avatar.urlHost, imageHost) else it.avatar)
                    }
                    new as MutableList<T>
                } else data
            }
            is Movie -> {
                data as MutableList<Movie>
                if (host.endsWith(".xyz")) return data
                val detailChange = data.any { it.link.urlHost != host }
                val imageChange = !TextUtils.isEmpty(imageHost) && data.any { it.imageUrl.urlHost != imageHost }

                return if (detailChange || imageChange) {
                    val new = data.mapTo(ArrayList(data.size)) {
                        if (it.link.urlHost.endsWith(".xyz")) it
                        else it.copy(link = if (detailChange) it.link.replace(it.link.urlHost, host) else it.link, imageUrl = if (imageChange) it.imageUrl.replace(it.imageUrl.urlHost, imageHost) else it.imageUrl)
                    }
                    //  collectCache?.put(Movie_Key, AppContext.gson.toJson(new))
                    new as MutableList<T>
                } else data
            }
            else -> data
        }
    }

    private inline fun <reified T : ILink> save() = Schedulers.io().scheduleDirect {
        val key = T::class.java.name
        collectCache?.put(key, getCollectData<T>().toJsonString())
    }

    private fun backUp(key: String): Boolean {
        return CollectManager.collectCache?.file(key)?.let {
            val bak = File(it.parent, "${key.hashCode()}.BAK")
            try {
                it.copyTo(bak, true)
                val name = if (key == Movie::class.java.name) "电影" else if (key == ActressInfo::class.java.name) "演员" else "链接"
                RxBus.post(CollectErrorEvent(key, "收藏${name}的数据格式错误,已转存至${bak.absolutePath}"))
                true
            } catch (e: Exception) {
                false
            }
        } ?: false

    }

}