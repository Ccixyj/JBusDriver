package me.jbusdriver.db.bean

import android.content.Context
import me.jbusdriver.common.AppContext
import me.jbusdriver.common.fromJson
import me.jbusdriver.common.toast
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.ui.activity.MovieDetailActivity
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.data.collect.ActressCollector
import me.jbusdriver.ui.data.collect.LinkCollector
import me.jbusdriver.ui.data.collect.MovieCollector
import java.util.*

/**
 * Created by Administrator on 2017/9/18 0018.
 */
data class History(val type: Int, val createTime: Date, val jsonStr: String, var isAll: Boolean = false) {
    var id: Int? = null


    fun move(context: Context) {

        when (type) {
            1 -> MovieDetailActivity.start(context, getLinkItem() as Movie, true)
            in 2..6 -> MovieListActivity.reloadFromHistory(context, this)
            else -> AppContext.instace.toast("没有可以跳转的界面")
        }

    }

    fun getLinkItem() = when (type) {
        1 -> AppContext.gson.fromJson<Movie>(jsonStr).apply {
            MovieCollector.checkUrls(mutableListOf(this)).first()
        }
        2 -> AppContext.gson.fromJson<ActressInfo>(jsonStr).apply {
            ActressCollector.checkUrls(mutableListOf(this)).first()
        }
        3 -> AppContext.gson.fromJson<Header>(jsonStr).apply {
            LinkCollector.checkUrls(mutableListOf(this)).first()
        }

        4 -> AppContext.gson.fromJson<Genre>(jsonStr).apply {
            LinkCollector.checkUrls(mutableListOf(this)).first()
        }
        5 -> AppContext.gson.fromJson<SearchLink>(jsonStr).apply {
            LinkCollector.checkUrls(mutableListOf(this)).first()
        }
        6 -> AppContext.gson.fromJson<PageLink>(jsonStr).apply {
            LinkCollector.checkUrls(mutableListOf(this)).first()
        }
        else -> error("$this has no matched class ")
    }
}


data class DBPage(val currentPage: Int, val totalPage: Int, val pageSize: Int = 20)

val DBPage.toPageInfo
    inline get() = PageInfo(currentPage, if (currentPage + 1 >= totalPage) totalPage else currentPage + 1)