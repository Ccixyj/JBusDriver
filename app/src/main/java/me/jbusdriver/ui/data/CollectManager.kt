package me.jbusdriver.ui.data

import android.os.Environment
import android.os.StatFs
import android.text.TextUtils
import com.umeng.analytics.MobclickAgent
import me.jbusdriver.common.*
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.CollectErrorEvent
import me.jbusdriver.mvp.bean.Movie
import java.io.File


/**
 * Created by Administrator on 2017/7/16.
 */
object CollectManager {

    const val Actress_Key = "Actress_Key"
    const val Movie_Key = "Movie_Key"
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
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val path = Environment.getExternalStorageDirectory()//获取SDCard根目录
            val stat = StatFs(path.path)
            val blockSize = stat.blockSize.toLong()
            val availableBlocks = stat.availableBlocks.toLong()
            return availableBlocks * blockSize
        } else {
            return -1
        }
    }

    val actress_data: MutableList<ActressInfo> by lazy { refreshActress() }

    private fun checkActressUrls(data: MutableList<ActressInfo>): MutableList<ActressInfo> {
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


    val movie_data: MutableList<Movie> by lazy { refreshMovie() }

    private fun checkMovieUrls(data: MutableList<Movie>): MutableList<Movie> {
        if (host.endsWith(".xyz")) return data
        val detailChange = data.any { it.detailUrl.urlHost != host }
        val imageChange = !TextUtils.isEmpty(imageHost) && data.any { it.imageUrl.urlHost != imageHost }

        return if (detailChange || imageChange) {
            val new = data.mapTo(ArrayList(data.size)) {
                if (it.detailUrl.urlHost.endsWith(".xyz")) it
                else it.copy(detailUrl = if (detailChange) it.detailUrl.replace(it.detailUrl.urlHost, host) else it.detailUrl, imageUrl = if (imageChange) it.imageUrl.replace(it.imageUrl.urlHost, imageHost) else it.imageUrl)
            }
            //  collectCache?.put(Movie_Key, AppContext.gson.toJson(new))
            new
        } else data
    }

    /*===========添加收藏=============*/
    fun addToCollect(actressInfo: ActressInfo): Boolean {
        return actress_data.let {
            if (it.any { it.link.urlPath == actressInfo.link.urlPath }) {
                AppContext.instace.toast("${actressInfo.name}已收藏")
                return false
            }
            actressInfo.tag = null
            it.add(0, actressInfo)
            saveActress()
            true
        }
    }

    fun addToCollect(movie: Movie): Boolean {
        return movie_data.let {
            if (it.any { it.code == movie.code }) {
                AppContext.instace.toast("${movie.title}已收藏")
                return false
            }
            it.add(0, movie)
            saveMovie()
            true
        }
    }

    /*===========是否收藏了=============*/
    fun has(act: ActressInfo): Boolean = actress_data.any { it.link.urlPath == act.link.urlPath }

    fun has(movie: Movie): Boolean = movie_data.any { it.code == movie.code }

    /*===========删除收藏=============*/
    fun removeCollect(act: ActressInfo): Boolean {
        actress_data.let {
            val res = it.remove(act) || (it.find { it.link.urlPath == act.link.urlPath }?.let { da -> it.remove(da) } ?: false)
            if (res) saveActress()
            return res
        }
    }

    fun removeCollect(movie: Movie): Boolean {
        movie_data.let {
            val res = it.remove(movie) || (it.find { it.code == movie.code }?.let { da -> it.remove(da) } ?: false)
            if (res) saveMovie()
            return res
        }
    }


    /* ======== needRefresh  =========== */
    fun refreshActress() = collectCache?.getAsString(Actress_Key)?.let {
        try {
            AppContext.gson.fromJson<MutableList<ActressInfo>>(it)?.let {
                checkActressUrls(it)
            }
        } catch (e: Exception) {
            backUp(Actress_Key)
            mutableListOf<ActressInfo>()
        }
    } ?: mutableListOf()

    fun refreshMovie() = collectCache?.getAsString(Movie_Key)?.let {
        try {
            AppContext.gson.fromJson<MutableList<Movie>>(it)?.let {
                checkMovieUrls(it)
            }
        } catch (e: Exception) {
            backUp(Movie_Key)
            mutableListOf<Movie>()
        }
    } ?: mutableListOf()


    /* ======== save  =========== */
    fun saveActress() = collectCache?.put(Actress_Key, AppContext.gson.toJson(actress_data))

    fun saveMovie() = collectCache?.put(Movie_Key, AppContext.gson.toJson(movie_data))

    /*back up*/

    fun backUp(key: String): Boolean {
        return CollectManager.collectCache?.file(key)?.let {
            val bak = File(it.parent, "${key.hashCode()}.BAK")
            try {
                it.copyTo(bak, true)
                val name = if (key == CollectManager.Movie_Key) "电影" else "演员"
                RxBus.post(CollectErrorEvent(key, "收藏${name}的数据格式错误,已转存至${bak.absolutePath}"))
                true
            } catch (e: Exception) {
                false
            }
        } ?: false

    }
}