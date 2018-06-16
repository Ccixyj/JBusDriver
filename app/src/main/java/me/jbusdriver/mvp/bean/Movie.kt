package me.jbusdriver.mvp.bean

import android.text.TextUtils
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.gson.annotations.SerializedName
import me.jbusdriver.base.ACache
import me.jbusdriver.base.KLog
import me.jbusdriver.base.toJsonString
import me.jbusdriver.base.urlHost
import me.jbusdriver.base.common.C
import me.jbusdriver.base.CacheLoader
import me.jbusdriver.db.bean.MovieCategory
import me.jbusdriver.http.JAVBusService
import org.jsoup.nodes.Document

/**
 * Created by Administrator on 2017/4/16.
 */
data class Movie(
        val title: String,
        val imageUrl: String,
        val code: String, //番号
        val date: String, //日期
        @SerializedName("detailUrl") override val link: String,
        val tags: List<String> = listOf()//标签,

) : MultiItemEntity, ILink {
    @Transient
    override var categoryId: Int = MovieCategory.id ?: 1


    override fun getItemType(): Int = if (isInValid) -1 else 0

    companion object {
        //图片url host 设置
        fun loadFromDoc(str: Document): List<Movie> {
            return str.select(".movie-box").mapIndexed { _, element ->
                Movie(
                        title = element.select("img").attr("title"),
                        imageUrl = element.select("img").attr("src"),
                        code = element.select("date").first().text(),
                        date = element.select("date").getOrNull(1)?.text() ?: "",
                        link = element.attr("href"),
                        tags = element.select(".item-tag").firstOrNull()?.children()?.map { it.text() }
                                ?: emptyList()
                )
            }.apply {

                val host = this.firstOrNull()?.imageUrl?.urlHost ?: ""
                KLog.d("put defaultImageUrlHost  for movie $host ")
                if (host.isNotBlank()) {
                    val key = if (host.endsWith("xyz")) "xyz" else "default"
                    val set = JAVBusService.defaultImageUrlHosts.getOrPut(key) {
                       hashSetOf()
                    }
                    if (host !in set) {
                        set.add(host)
                        CacheLoader.acache.put(C.Cache.IMG_HOSTS, JAVBusService.defaultImageUrlHosts.toJsonString(), ACache.TIME_DAY)
                    }
                }

            }
        }

        fun newPageMovie(page: Int, pages: List<Int>) = Movie(page.toString(), pages.joinToString("#"), "", "", "")
    }
}

val Movie.saveKey
    inline get() = code.trim() + "_" + date.trim()

private val Movie.isInValid
    inline get() = TextUtils.isEmpty(code) && TextUtils.isEmpty(link)
