package me.jbusdriver

import me.jbusdriver.common.ACache
import me.jbusdriver.common.AppContext
import me.jbusdriver.common.fromJson
import me.jbusdriver.common.toast
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.Movie
import java.util.*

/**
 * Created by Administrator on 2017/7/16.
 */
object CollectManager {

    private const val Actress_Key = "Actress_Key"
    private const val Movie_Key = "Movie_Key"

    private val collectCache by lazy {   ACache.get(AppContext.instace,"collect")}


     val actress_data:LinkedList<ActressInfo>
        get() = collectCache.getAsString(Actress_Key)?.let { AppContext.gson.fromJson<LinkedList<ActressInfo>>(it) }  ?: LinkedList()

     val movie_data:LinkedList<Movie>
        get() =collectCache.getAsString(Movie_Key)?.let { AppContext.gson.fromJson<LinkedList<Movie>>(it) }  ?:  LinkedList()

    fun addToCollect(actressInfo: ActressInfo){
        actress_data.let {
            if (it.any { it == actressInfo}){
                AppContext.instace.toast("${actressInfo.name}已收藏")
                return
            }
            it.addFirst(actressInfo)
            collectCache.put(Actress_Key,AppContext.gson.toJson(it))
        }
    }

    fun addToCollect(movie: Movie){
        movie_data.let {
            if (it.any { it == movie}){
                AppContext.instace.toast("${movie.title}已收藏")
                return
            }
            it.addFirst(movie)
            collectCache.put(Movie_Key,AppContext.gson.toJson(it))
        }
    }
}