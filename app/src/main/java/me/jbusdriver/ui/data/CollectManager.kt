package me.jbusdriver.ui.data

import android.net.Uri
import android.os.Environment
import me.jbusdriver.common.*
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.Movie
import java.io.File


/**
 * Created by Administrator on 2017/7/16.
 */
object CollectManager {

    private const val Actress_Key = "Actress_Key"
    private const val Movie_Key = "Movie_Key"
    private val host: String by lazy { JAVBusService.defaultFastUrl }
    private val collectCache by lazy {
        val cacheDir = if (Environment.isExternalStorageEmulated()) File((Environment.getExternalStorageDirectory().absolutePath + File.separator + AppContext.instace.packageName + File.separator + "collect")) else (AppContext.instace.externalCacheDir ?: AppContext.instace.cacheDir)
        ACache.get(cacheDir)
    }


    init {
        KLog.d("CollectManager :" + Uri.parse("https://www.javbus3.com").path)
        KLog.d("CollectManager :" + Uri.parse("http://wx.cfzxzz.com/ucenter/entrust/entrustlist.html").path)

    }

    val actress_data: MutableList<ActressInfo>
        get() = collectCache.getAsString(Actress_Key)?.let {
            AppContext.gson.fromJson<MutableList<ActressInfo>>(it)?.let {
                checkActressUrls(it)
            }
        } ?: mutableListOf()

    private fun checkActressUrls(data: MutableList<ActressInfo>): MutableList<ActressInfo> {
        return if (data.any { it.link.urlHost != host }) {
            val new = data.mapTo(ArrayList(data.size)) {
                it.copy(link = it.link.replace(it.link.urlHost, host))
            }
            collectCache.put(Actress_Key, AppContext.gson.toJson(new))
            new
        } else data
    }


    val movie_data: MutableList<Movie>
        get() = collectCache.getAsString(Movie_Key)?.let {
            AppContext.gson.fromJson<MutableList<Movie>>(it)?.let {
                checkMovieUrls(it)
            }
        } ?: mutableListOf()

    private fun checkMovieUrls(data: MutableList<Movie>): MutableList<Movie> {
        return if (data.any { it.detailUrl.urlHost != host }) {
            val new = data.mapTo(ArrayList(data.size)) {
                it.copy(detailUrl = it.detailUrl.replace(it.detailUrl.urlHost, host))
            }
            collectCache.put(Movie_Key, AppContext.gson.toJson(new))
            new
        } else data
    }

    fun addToCollect(actressInfo: ActressInfo): Boolean {
        return actress_data.let {
            if (it.any { it == actressInfo }) {
                AppContext.instace.toast("${actressInfo.name}已收藏")
                return false
            }
            it.add(0, actressInfo)
            collectCache.put(Actress_Key, AppContext.gson.toJson(it))
            true
        }
    }

    fun addToCollect(movie: Movie): Boolean {
        return movie_data.let {
            if (it.any { it == movie }) {
                AppContext.instace.toast("${movie.title}已收藏")
                return false
            }
            it.add(0, movie)
            collectCache.put(Movie_Key, AppContext.gson.toJson(it))
            true
        }
    }

    fun has(act: ActressInfo): Boolean = actress_data.any { it.link == act.link }
    fun has(movie: Movie): Boolean = movie_data.any { it.code == movie.code }
    fun removeCollect(act: ActressInfo): Boolean {
        actress_data.let {
            val res = it.remove(act)
            if (res) collectCache.put(Actress_Key, AppContext.gson.toJson(it))
            return res
        }
    }

    fun removeCollect(movie: Movie): Boolean {
        movie_data.let {
            val res = it.remove(movie)
            if (res) collectCache.put(Movie_Key, AppContext.gson.toJson(it))
            return res
        }
    }
}