package me.jbusdriver.mvp.bean

/**
 * Created by Administrator on 2017/4/9.
 */

data class PageInfo(val activePage: Int = 0, val nextPage: Int = 0)

val PageInfo.hasNext
    inline get() = activePage < nextPage

data class Movie(
        val title: String,
        val imageUrl: String,
        val code: String, //饭好
        val date: String, //日期
        val tags: List<String> = listOf()//标签
)