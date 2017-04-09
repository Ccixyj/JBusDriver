package me.jbusdriver.mvp.bean

/**
 * Created by Administrator on 2017/4/9.
 */

data class PageItem(val page: Int = 0, val url: String = "")

data class Page(val activePage: PageItem = PageItem(), val nextPage: PageItem = PageItem())

val Page.hasNext
    inline get() = activePage.page < nextPage.page

data class Movie(
        val title: String,
        val code: String,
        val coverUrl: String,
        val date: String,
        val hot: String)