package me.jbusdriver.mvp.bean

import me.jbusdriver.base.toJsonString
import me.jbusdriver.base.urlPath
import me.jbusdriver.db.bean.AllFirstParentDBCategoryGroup
import me.jbusdriver.db.bean.ICollectCategory
import me.jbusdriver.db.bean.LinkCategory
import me.jbusdriver.db.bean.LinkItem
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.ui.data.enums.SearchType
import java.io.Serializable
import java.util.*

/**
 * Created by Administrator on 2017/4/9.
 */

const val Expand_Type_Head = 0
const val Expand_Type_Item = 1


interface ILink : ICollectCategory, Serializable {
    val link: String
}

val ILink.des: String
    inline get() = when (this) {
        is Header -> "$name $value"
        is Genre -> "类别 $name"
        is ActressInfo -> "演员 $name"
        is me.jbusdriver.mvp.bean.Movie -> "$code $title"
        is SearchLink -> "搜索 ${type.title} $query"
        is PageLink -> "$title 第 $page 页" /*${if (isAll) "全部" else "已有种子"}电影*/
        else -> error(" $this has no matched class for des")
    }

const val MovieDBType = 1
const val ActressDBType = 2
const val HeaderDBType = 3
const val GenreDBType = 4
const val SearchLinkDBType = 5
const val PageLinkDBType = 6

val AllDBType by lazy { listOf(MovieDBType, ActressDBType, HeaderDBType, GenreDBType, SearchLinkDBType, PageLinkDBType) }

val ILink.DBtype: Int
    inline get() = when (this) {
        is Movie -> MovieDBType
        is ActressInfo -> ActressDBType
        is Header -> HeaderDBType
        is Genre -> GenreDBType
        is SearchLink -> SearchLinkDBType
        is PageLink -> PageLinkDBType
        else -> error(" $this has no matched class for des")
    }
val ILink.uniqueKey: String
    inline get() = when (this) {
        is SearchLink -> query
        else -> link.urlPath
    }

fun ILink.convertDBItem() = LinkItem(this.DBtype, Date(), this.uniqueKey, this.toJsonString(),
        when {
            this is ICollectCategory && this.categoryId > 0 -> categoryId
            else -> AllFirstParentDBCategoryGroup[this.DBtype]?.id ?: LinkCategory.id ?: -1
        })

data class PageLink(val page: Int, val title: String /*XX类型*/, override val link: String) : ILink {
    @Transient
    override var categoryId: Int = LinkCategory.id ?: 10
}

data class PageInfo(val activePage: Int = 0, val nextPage: Int = 0,
                    val referPages: List<Int> = listOf())

val PageInfo.hasNext
    inline get() = activePage < nextPage


data class SearchLink(val type: SearchType, var query: String) : ILink {
    @Transient
    override var categoryId: Int = LinkCategory.id ?: 10
        set(value) {}
    override val link: String
        get() = "${JAVBusService.defaultFastUrl}${type.urlPathFormater.format(query)}"

}


data class UpdateBean(val versionCode: Int, val versionName: String, val url: String, val desc: String)
data class NoticeBean(val id: Int, val content: String? = null)


