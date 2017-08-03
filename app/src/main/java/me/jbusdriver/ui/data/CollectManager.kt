package me.jbusdriver.ui.data

import android.os.Environment
import android.os.StatFs
import android.text.TextUtils
import me.jbusdriver.common.*
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.Movie
import java.io.File


/**
 * Created by Administrator on 2017/7/16.
 */
object CollectManager {

    init {
        val pathSuffix = File.separator + "collect"
        KLog.i(AppContext.instace.externalCacheDir.absolutePath + pathSuffix)
        KLog.i(AppContext.instace.cacheDir.absolutePath + pathSuffix)
        KLog.i(getAvailableExternalMemorySize().formatFileSize())
    }

    private const val Actress_Key = "Actress_Key"
    private const val Movie_Key = "Movie_Key"
    private val host: String by lazy { JAVBusService.defaultFastUrl }
    private val imageHost: String by lazy { JAVBusService.defaultImageUrlHost }
    private val collectCache by lazy {
        val pathSuffix = File.separator + "collect"
        var isSdCard = false
        val collectDir =
                if (Environment.isExternalStorageEmulated()) {
                    isSdCard = true
                    Environment.getExternalStorageDirectory().absolutePath + File.separator + AppContext.instace.packageName + pathSuffix
                } else {
                    (AppContext.instace.externalCacheDir.absolutePath ?: AppContext.instace.cacheDir.absolutePath) + pathSuffix
                }
        try {
            val target = File(collectDir)
            //可能存在旧的,复制到新的目录下去并删除
            ACache.get(target).apply {
                if (isSdCard) {
                    if ((target.list()?.size ?: -1) > 0) return@apply
                    if (getAvailableExternalMemorySize() < MB * 100) {
                        AppContext.instace.toast("sd卡可用空间不足100M")
                    }
                    val fileOld = File(AppContext.instace.cacheDir.absolutePath + pathSuffix)
                    val fileOld2 = File(AppContext.instace.externalCacheDir.absolutePath + pathSuffix)
                    if (fileOld.exists() && (fileOld.list()?.size ?: -1) > 0) {
                        fileOld.copyRecursively(target)
                        fileOld.deleteRecursively()
                    } else if (fileOld2.exists() && (fileOld2.list()?.size ?: -1) > 0) {
                        fileOld2.copyRecursively(target)
                        fileOld2.deleteRecursively()
                    }
                }
            }

        } catch(e: Exception) {
            AppContext.instace.toast("收藏目录创建失败,请检查app是否有sd卡操作权限")
            ACache.get(AppContext.instace.cacheDir)
            null
        }
    }

    fun getAvailableExternalMemorySize(): Long {
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

    val actress_data: MutableList<ActressInfo> by lazy {
        collectCache?.getAsString(Actress_Key)?.let {
            AppContext.gson.fromJson<MutableList<ActressInfo>>(it)?.let {
                checkActressUrls(it)
            }
        } ?: mutableListOf()

    }

    private fun checkActressUrls(data: MutableList<ActressInfo>): MutableList<ActressInfo> {
        val linkChange = data.any { it.link.urlHost != host }
        val imageChange = !TextUtils.isEmpty(imageHost) && data.any { it.avatar.urlHost != imageHost && !it.avatar.endsWith("nowprinting.gif") }
        return if (linkChange || imageChange) {
            val new = data.mapTo(ArrayList(data.size)) {
                it.copy(link = if (linkChange) it.link.replace(it.link.urlHost, host) else it.link, avatar = if (imageChange) it.avatar.replace(it.avatar.urlHost, imageHost) else it.avatar)
            }
            collectCache?.put(Actress_Key, AppContext.gson.toJson(new))
            new
        } else data
    }


    val movie_data: MutableList<Movie> by lazy {
        collectCache?.getAsString(Movie_Key)?.let {
            AppContext.gson.fromJson<MutableList<Movie>>(it)?.let {
                checkMovieUrls(it)
            }
        } ?: mutableListOf()

    }

    private fun checkMovieUrls(data: MutableList<Movie>): MutableList<Movie> {
        val detailChange = data.any { it.detailUrl.urlHost != host }
        val imageChange = !TextUtils.isEmpty(imageHost) && data.any { it.imageUrl.urlHost != imageHost }

        return if (detailChange || imageChange) {
            val new = data.mapTo(ArrayList(data.size)) {
                it.copy(detailUrl = if (detailChange) it.detailUrl.replace(it.detailUrl.urlHost, host) else it.detailUrl, imageUrl = if (imageChange) it.imageUrl.replace(it.imageUrl.urlHost, imageHost) else it.imageUrl)
            }
            collectCache?.put(Movie_Key, AppContext.gson.toJson(new))
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
            it.add(0, actressInfo)
            collectCache?.put(Actress_Key, AppContext.gson.toJson(it))
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
            collectCache?.put(Movie_Key, AppContext.gson.toJson(it))
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
            if (res) collectCache?.put(Actress_Key, AppContext.gson.toJson(it))
            return res
        }
    }

    fun removeCollect(movie: Movie): Boolean {
        movie_data.let {
            val res = it.remove(movie) || (it.find { it.code == movie.code }?.let { da -> it.remove(da) } ?: false)
            if (res) collectCache?.put(Movie_Key, AppContext.gson.toJson(it))
            return res
        }
    }


    /* ========  =========== */
}