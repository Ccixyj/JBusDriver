package me.jbusdriver.mvp.bean

import android.text.TextUtils
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.gson.annotations.SerializedName
import me.jbusdriver.common.urlHost
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.ui.data.enums.DataSourceType
import org.jsoup.nodes.Document

/**
 * Created by Administrator on 2017/4/16.
 */
data class Movie(
        val type: DataSourceType,
        val title: String,
        val imageUrl: String,
        val code: String, //番号
        val date: String, //日期
        @SerializedName("detailUrl") override val link: String,
        val tags: List<String> = listOf()//标签,

) : MultiItemEntity, ILink {

    override fun getItemType(): Int = if (isInValid) -1 else 0

    companion object {
        //图片url host 设置
        fun loadFromDoc(type: DataSourceType, str: Document): List<Movie> {
            return str.select(".movie-box").mapIndexed { _, element ->
                Movie(
                        type = type,
                        title = element.select("img").attr("title"),
                        imageUrl = element.select("img").attr("src"),
                        code = element.select("date").first().text(),
                        date = element.select("date").getOrNull(1)?.text() ?: "",
                        link = element.attr("href"),
                        tags = element.select(".item-tag").firstOrNull()?.children()?.map { it.text() } ?: emptyList()
                )
            }.apply {
                val host = this.firstOrNull()?.imageUrl?.urlHost ?: ""
                if (host.isNotBlank() && !host.endsWith(".xyz")) {
                    JAVBusService.defaultImageUrlHost = host
                }

            }
        }

        fun newPageMovie(page: Int, pages: List<Int>, type: DataSourceType = DataSourceType.CENSORED) = Movie(type, page.toString(), pages.joinToString("#"), "", "", "")
    }
}

val Movie.detailSaveKey
    inline get() = code + "_" + date

private val Movie.isInValid
    inline get() = TextUtils.isEmpty(code) && TextUtils.isEmpty(link)
