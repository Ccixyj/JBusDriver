package me.jbusdriver.mvp.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

/**
 * Created by Administrator on 2017/4/16.
 */
data class Movie(val title: String,
                 val imageUrl: String,
                 val code: String, //番号
                 val date: String, //日期
                 val detailUrl: String,
                 val tags: List<String> = listOf()//标签
) : MultiItemEntity, Serializable {
    override fun getItemType(): Int = 0
}

val Movie.saveKey
    inline get() = code + date
