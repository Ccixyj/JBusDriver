package me.jbusdriver.db.bean

import android.content.Context
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.ui.activity.MovieDetailActivity
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.data.DataSourceType
import java.util.*

/**
 * Created by Administrator on 2017/9/18 0018.
 */
data class History(val des: String, val url: String, val type: Int, val createTime: Date, val img: String? = null) {
    var id: Int? = null

    /**
     * inline get() = when (this) {
    is Movie -> 1
    is ActressInfo -> 2
    is Header -> 3
    is Genre -> 4
    is SearchLink -> 5
    is PageLink -> 6
    else -> error(" $this has no matched class for des")
    }
     */
    fun moveto(context: Context) {
        when (type) {
            1 -> {
                val ss = des.split(" ")
                MovieDetailActivity.start(context, Movie(DataSourceType.CENSORED, ss[0], img ?: "", ss[1], "", url))
            }

            2 -> {
                val ss = des.split(" ")
                MovieListActivity.start(context, ActressInfo(ss[1], img ?: "", url))
            }

            3 -> {
                val ss = des.split(" ")

                MovieListActivity.start(context, Header(ss[0], ss[1], url))
            }

            4 -> {
                val ss = des.split(" ")
                MovieListActivity.start(context, Genre(ss[1], url))
            }
            5 -> {
                val ss = des.split(" ")
                MovieListActivity.start(context, Genre(ss[1], url))
            }

            6 -> {
                val isAll = des.endsWith("全部电影")
//                val s = url.split("/").last()
//                val page = s.toIntOrNull()?.let { it } ?: 1
//                val replace = if (page > 1) url.replace("$page", "") else url
                MovieListActivity.start(context, PageLink(1, des, url, isAll))
            }
        }
    }
}


data class DBPage(val currentPage: Int, val totalPage: Int, val pageSize: Int = 20)

val DBPage.toPageInfo
    inline get() = PageInfo(currentPage, if (currentPage + 1 >= totalPage) totalPage else currentPage + 1)