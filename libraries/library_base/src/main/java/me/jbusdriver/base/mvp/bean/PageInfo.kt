package me.jbusdriver.base.mvp.bean

data class PageInfo(val activePage: Int = 0, val nextPage: Int = 0,
                    val referPages: List<Int> = listOf())

val PageInfo.hasNext
    inline get() = activePage < nextPage