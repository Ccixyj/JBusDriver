package me.jbusdriver.db.bean

import android.content.ContentValues
import android.content.Context
import me.jbusdriver.base.*
import me.jbusdriver.common.JBus
import me.jbusdriver.db.CategoryTable
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.ui.activity.MovieDetailActivity
import me.jbusdriver.ui.activity.MovieListActivity
import java.util.*


data class Category(val name: String, val pid: Int = -1, val tree: String, var order: Int = 0) {
    var id: Int? = null

    @delegate:Transient
    val depth: Int by lazy { tree.split("/").filter { it.isNotBlank() }.size }

    fun cv(update: Boolean = false): ContentValues = ContentValues().also {
        if (id != null || update) it.put(CategoryTable.COLUMN_ID, id)
        it.put(CategoryTable.COLUMN_NAME, name)
        it.put(CategoryTable.COLUMN_P_ID, pid)
        it.put(CategoryTable.COLUMN_TREE, tree)
        it.put(CategoryTable.COLUMN_ORDER, order)
    }

    override fun equals(other: Any?) =
            other?.let { (it as? Category)?.id == this.id } ?: false

    fun equalAll(other: Category?) = other?.let { it.id == this.id && it.name == this.name && it.pid == this.pid && it.tree == this.tree }
            ?: false
}

/**
 * 预留 [3..9]的分类
 */
val MovieCategory = Category("默认电影分类", -1, "1/", Int.MAX_VALUE).apply { id = 1 }
val ActressCategory = Category("默认演员分类", -1, "2/", Int.MAX_VALUE).apply { id = 2 }
val LinkCategory = Category("默认链接分类", -1, "10/", Int.MAX_VALUE).apply { id = 10 }
val AllFirstParentDBCategoryGroup by lazy { arrayMapof(1 to MovieCategory, 2 to ActressCategory, 10 to LinkCategory) }


data class LinkItem(val type: Int, val createTime: Date, val key: String, val jsonStr: String, var categoryId: Int = -1) {
    var id: Int? = null
    fun getLinkValue() = doGet(type, jsonStr).also {
        if (it is ICollectCategory) {
            it.categoryId = this.categoryId
        }
    }
}

data class History(val type: Int, val createTime: Date, val jsonStr: String, var isAll: Boolean = false) {
    var id: Int? = null


    fun move(context: Context) {

        when (type) {
            1 -> MovieDetailActivity.start(context, getLinkItem() as Movie, true)
            in 2..6 -> MovieListActivity.reloadFromHistory(context, this)
            else -> JBus.toast("没有可以跳转的界面")
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
    val isXyz = data.link.urlHost.endsWith("xyz")
    if (isXyz) return@let data
    KLog.d("check type : $type  #$data hosts: ${JAVBusService.defaultImageUrlHosts}")
    val host: String by lazy { JAVBusService.defaultFastUrl }
//    val imageHost: Set<String> by lazy {
//        if (!isXyz) JAVBusService.defaultImageUrlHosts["default"] ?: mutableSetOf()
//        else JAVBusService.defaultImageUrlHosts["xyz"] ?: mutableSetOf()
//    }
    return@let when (data) {
        is Movie -> {
            val linkChange = data.link.urlHost != host
//            val imageChange =  imageHost.isNotEmpty() && data.imageUrl.urlHost !in imageHost
//            if (imageChange){
//                imageHost.map { it + data.imageUrl.urlPath }.filter { Glide.with(JBus).asFile().load(it).preload()  != null}
//            }
            data.copy(link = if (linkChange) data.link.replace(data.link.urlHost, host) else data.link, imageUrl = /*if (imageChange) data.imageUrl.replace(data.imageUrl.urlHost, imageHost) else*/ data.imageUrl)
        }
        is ActressInfo -> {
            val linkChange = data.link.urlHost != host
//            val imageChange =  imageHost.isNotEmpty() && data.avatar.urlHost !in imageHost && !data.avatar.endsWith("nowprinting.gif")
            data.copy(link = if (linkChange) data.link.replace(data.link.urlHost, host) else data.link, avatar = /*if (imageChange) data.avatar.replace(data.avatar.urlHost, imageHost) else*/ data.avatar)
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
    }.apply {
        KLog.d("check link : $this")
    }

}


interface ICollectCategory {
    var categoryId: Int
}


data class DBPage(val currentPage: Int, val totalPage: Int, val pageSize: Int = 20)

val DBPage.toPageInfo
    inline get() = PageInfo(currentPage, Math.min(currentPage + 1, totalPage))