package me.jbusdriver.mvp.bean

import android.text.TextUtils
import com.chad.library.adapter.base.entity.MultiItemEntity
import me.jbusdriver.common.KLog
import me.jbusdriver.common.urlHost
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.ui.data.DataSourceType
import org.jsoup.nodes.Document
import java.io.Serializable

/**
 * Created by Administrator on 2017/4/16.
 */
data class Movie(
        val type: DataSourceType,
        val title: String,
        val imageUrl: String,
        val code: String, //番号
        val date: String, //日期
        val detailUrl: String,
        val tags: List<String> = listOf()//标签,

) : MultiItemEntity, Serializable {

    override fun getItemType(): Int = if (isInValid) -1 else 0

    companion object {
        //图片url host 设置
        fun loadFromDoc(type: DataSourceType, str: Document): List<Movie> {
            return str.select(".movie-box").mapIndexed { index, element ->
                KLog.d(element)
                Movie(
                        type = type,
                        title = element.select("img").attr("title"),
                        imageUrl = element.select("img").attr("src"),
                        code = element.select("date").first().text(),
                        date = element.select("date").getOrNull(1)?.text() ?: "",
                        detailUrl = element.attr("href"),
                        tags = element.select(".item-tag").firstOrNull()?.children()?.map { it.text() } ?: emptyList()
                )
            }.apply {
                val host = this.firstOrNull()?.imageUrl?.urlHost ?: ""
                if (host.isNotBlank() && !host.endsWith(".xyz")) {
                    JAVBusService.defaultImageUrlHost = host
                }

            }
        }

        fun newPageMovie(page: Int, type: DataSourceType = DataSourceType.CENSORED) = Movie(type, page.toString(), "", "", "", "")
    }
}

val Movie.detailSaveKey
    inline get() = code + "_" + date

private val Movie.isInValid
    inline get() = TextUtils.isEmpty(code) && TextUtils.isEmpty(detailUrl)
