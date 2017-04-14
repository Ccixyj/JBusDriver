package me.jbusdriver.mvp.bean

import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 * Created by Administrator on 2017/4/9.
 */

data class PageInfo(val activePage: Int = 0, val nextPage: Int = 0)

val PageInfo.hasNext
    inline get() = activePage < nextPage

data class Movie(
        val title: String,
        val imageUrl: String,
        val code: String, //番号
        val date: String, //日期
        val detail: String,
        val tags: List<String> = listOf()//标签
) : MultiItemEntity {
    override fun getItemType(): Int = 0
}


