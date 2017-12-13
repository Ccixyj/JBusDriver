package me.jbusdriver.db.bean

import android.content.ContentValues
import android.content.Context
import com.google.gson.annotations.Expose
import me.jbusdriver.common.AppContext
import me.jbusdriver.common.fromJson
import me.jbusdriver.common.toast
import me.jbusdriver.db.CategoryTable
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.ui.activity.MovieDetailActivity
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.data.collect.ActressCollector
import me.jbusdriver.ui.data.collect.LinkCollector
import me.jbusdriver.ui.data.collect.MovieCollector
import java.util.*


data class Category(val name: String, val pid: Int = -1, val tree: String) {
    var id: Int? = null

    @delegate:Transient
    val depth: Int by lazy { tree.split("/").filter { it.isNotBlank() }.size }

    fun cv(): ContentValues = ContentValues().also {
        it.put(CategoryTable.COLUMN_NAME, name)
        it.put(CategoryTable.COLUMN_P_ID, pid)
        it.put(CategoryTable.COLUMN_TREE, tree)
    }

    override fun equals(other: Any?) =
            other?.let { (it as? Category)?.id == this.id } ?: false
}

val MovieCategory = Category("默认电影分类", -1, "/").apply { id = 1 }
val ActressCategory = Category("默认演员分类", -1, "/").apply { id = 2 }
val LinkCategory = Category("默认链接分类", -1, "/").apply { id = 3 }


data class LinkItem(val type: Int, val createTime: Date, val key: String, val jsonStr: String, var categoryId: Int = -1) {
    var id: Int? = null
    fun getLinkValue() = doGet(type, jsonStr).apply {
        if (this is ICollectCategory) {
            this.categoryId = this@LinkItem.categoryId
        }
    }
}

data class History(val type: Int, val createTime: Date, val jsonStr: String, var isAll: Boolean = false) {
    var id: Int? = null


    fun move(context: Context) {

        when (type) {
            1 -> MovieDetailActivity.start(context, getLinkItem() as Movie, true)
            in 2..6 -> MovieListActivity.reloadFromHistory(context, this)
            else -> AppContext.instace.toast("没有可以跳转的界面")
        }

    }

    fun getLinkItem() = doGet(type, jsonStr)
}

private fun doGet(type: Int, jsonStr: String) = when (type) {
    1 -> AppContext.gson.fromJson<Movie>(jsonStr).let {
        MovieCollector.checkUrls(mutableListOf(it)).first()
    }
    2 -> AppContext.gson.fromJson<ActressInfo>(jsonStr).let {
        ActressCollector.checkUrls(mutableListOf(it)).first()
    }
    3 -> AppContext.gson.fromJson<Header>(jsonStr).let {
        LinkCollector.checkUrls(mutableListOf(it)).first()
    }

    4 -> AppContext.gson.fromJson<Genre>(jsonStr).let {
        LinkCollector.checkUrls(mutableListOf(it)).first()
    }
    5 -> AppContext.gson.fromJson<SearchLink>(jsonStr).let {
        LinkCollector.checkUrls(mutableListOf(it)).first()
    }
    6 -> AppContext.gson.fromJson<PageLink>(jsonStr).let {
        LinkCollector.checkUrls(mutableListOf(it)).first()
    }
    else -> error("$type : $jsonStr has no matched class ")
}

interface ICollectCategory {
    var categoryId: Int
}


data class DBPage(val currentPage: Int, val totalPage: Int, val pageSize: Int = 20)

val DBPage.toPageInfo
    inline get() = PageInfo(currentPage, if (currentPage + 1 >= totalPage) totalPage else currentPage + 1)