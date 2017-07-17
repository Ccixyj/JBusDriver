package me.jbusdriver.common

import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.Movie

/**
 * Created by Administrator on 2017/7/16.
 */
object CollectManager {

    private const val Actress_Key = "Actress_Key"
    private const val Movie_Key = "Movie_Key"

    private val collectCache by lazy { ACache.get(AppContext.instace, "collect") }


    val actress_data: MutableList<ActressInfo>
        get() = collectCache.getAsString(Actress_Key)?.let { AppContext.gson.fromJson<MutableList<ActressInfo>>(it) } ?: mutableListOf()

    val movie_data: MutableList<Movie>
        get() = collectCache.getAsString(Movie_Key)?.let { AppContext.gson.fromJson<MutableList<Movie>>(it) } ?: mutableListOf()

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