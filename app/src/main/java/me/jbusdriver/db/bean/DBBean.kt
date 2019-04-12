package me.jbusdriver.db.bean

import android.content.Context
import com.umeng.analytics.MobclickAgent
import me.jbusdriver.base.*
import me.jbusdriver.base.mvp.bean.PageInfo
import me.jbusdriver.common.bean.ILink
import me.jbusdriver.common.isEndWithXyzHost
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.ui.activity.MovieDetailActivity
import me.jbusdriver.ui.activity.MovieListActivity
import java.util.*


data class LinkItem(
    val type: Int,
    val createTime: Date,
    val key: String,
    val jsonStr: String,
    var categoryId: Int = -1
) {
    var id: Int? = null
    fun getLinkValue(): ILink? {
        return kotlin.runCatching {
            val link = doGet(type, jsonStr)
            link.categoryId = this.categoryId
            link
        }.onFailure {
            val error = "error getLinkValue : $this"
            KLog.w(error)
            MobclickAgent.reportError(JBusManager.context, error)

        }.getOrNull()
    }
}

data class History(val type: Int, val createTime: Date, val jsonStr: String, var isAll: Boolean = false) {
    var id: Int? = null


    fun move(context: Context) {

        when (type) {
            1 -> MovieDetailActivity.start(context, getLinkItem() as Movie, true)
            in 2..6 -> MovieListActivity.reloadFromHistory(context, this)
            else -> toast("没有可以跳转的界面")
        }

    }

    fun getLinkItem() = doGet(type, jsonStr)
}

private fun doGet(type: Int, jsonStr: String) = when (type) {
    1 -> GSON.fromJson<Movie>(jsonStr)
    2 -> GSON.fromJson<ActressInfo>(jsonStr)
    3 -> GSON.fromJson<Header>(jsonStr)
    4 -> GSON.fromJson<Genre>(jsonStr)
    5 -> GSON.fromJson<SearchLink>(jsonStr)
    6 -> GSON.fromJson<PageLink>(jsonStr)
    else -> error("$type : $jsonStr has no matched class ")
}.let { data ->
    val isXyz = data.link.urlHost.isEndWithXyzHost
    if (isXyz) return@let data
    val host: String by lazy { JAVBusService.defaultFastUrl }
    return@let when (data) {
        is Movie -> {
            val linkChange = data.link.urlHost != host
            data.copy(
                link = if (linkChange) data.link.replace(data.link.urlHost, host) else data.link,
                imageUrl = /*if (imageChange) data.imageUrl.replace(data.imageUrl.urlHost, imageHost) else*/ data.imageUrl,
                tags = emptyList()
            )
        }
        is ActressInfo -> {
            val linkChange = data.link.urlHost != host
            data.copy(
                link = if (linkChange) data.link.replace(data.link.urlHost, host) else data.link,
                avatar = /*if (imageChange) data.avatar.replace(data.avatar.urlHost, imageHost) else*/ data.avatar,
                tag = null
            )
        }
        else -> {
            val linkChange = data.link.urlHost != host
            if (linkChange) {
                when (data) {
                    is Header -> data.copy(link = data.link.replace(data.link.urlHost, host))
                    is Genre -> data.copy(link = data.link.replace(data.link.urlHost, host))
                    else -> data
                }
            } else data
        }
    }
}


data class DBPage(val currentPage: Int, val totalPage: Int, val pageSize: Int = 20)

val DBPage.toPageInfo
    inline get() = PageInfo(currentPage, Math.min(currentPage + 1, totalPage))