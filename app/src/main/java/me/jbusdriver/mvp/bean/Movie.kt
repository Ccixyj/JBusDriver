package me.jbusdriver.mvp.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import me.jbusdriver.common.KLog
import org.jsoup.nodes.Document
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

    companion object {
        fun loadFromDoc(str: Document): List<Movie> {
            return str.select(".movie-box").map { element ->
                KLog.d(element)
                Movie(title = element.select("img").attr("title"),
                        imageUrl = element.select("img").attr("src"),
                        code = element.select("date").first().text(),
                        date = element.select("date").getOrNull(1)?.text() ?: "",
                        detailUrl = element.attr("href"),
                        tags = element.select(".item-tag").firstOrNull()?.children()?.map { it.text() } ?: emptyList()
                )
            }
        }
    }
}

val Movie.saveKey
    inline get() = code + date
