package me.jbusdriver.mvp.bean

/**
 * Created by Administrator on 2017/4/9.
 */

data class PageInfo(val activePage: Int = 0, val nextPage: Int = 0,
                    val activePath: String = "",
                    val nextPath: String = "")

val PageInfo.hasNext
    inline get() = activePage < nextPage



