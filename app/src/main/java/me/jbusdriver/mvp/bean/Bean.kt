package me.jbusdriver.mvp.bean

import me.jbusdriver.http.JAVBusService
import me.jbusdriver.ui.data.SearchType

/**
 * Created by Administrator on 2017/4/9.
 */

data class PageInfo(val activePage: Int = 0, val nextPage: Int = 0,
                    val activePath: String = "",
                    val nextPath: String = "",
                    val pages: List<Int> = listOf())

val PageInfo.hasNext
    inline get() = activePage < nextPage


data class SearchLink(val type: SearchType, var query: String) : ILink {

    override val link: String
        get() = "${JAVBusService.defaultFastUrl}${type.urlPathFormater.format(query)}"

}

data class UpdateBean(val versionCode: Int, val versionName: String, val url: String, val desc: String)
data class NoticeBean(val id: Int, val content: String? = null)
