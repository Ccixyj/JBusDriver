package me.jbusdriver.mvp.bean

import android.text.TextUtils
import me.jbusdriver.base.KLog
import me.jbusdriver.base.urlHost
import me.jbusdriver.db.bean.ActressCategory
import me.jbusdriver.db.bean.LinkCategory
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.Serializable

/**
 * Created by Administrator on 2017/4/16.
 */

data class MovieDetail(val title: String,
                       val content: String,
                       val cover: String, //封面
                       val headers: List<Header>,
        /*
          val code: String,
        val publishDate: String,
         val director: Pair<String, String>, //导演
        val studio: Pair<String, String>, //製作商
        val label: Pair<String, String>, //發行商
        val series: Pair<String, String>, //系列*/
                       val genres: List<Genre>, //類別
                       val actress: List<ActressInfo>, //出演
                       val imageSamples: List<ImageSample>, //截圖
                       val relatedMovies: List<Movie> //推薦
        //  val magnets: MutableList<Magnet> = mutableListOf() //磁力链接
) {
    companion object {



        fun parseDetails(doc: Document): MovieDetail {
            KLog.d("start parseDetails ")
            val roeMovie = doc.select("[class=row movie]")
            val title = doc.select(".container h3").text()
            val cover = roeMovie.select(".bigImage").attr("href")

            val headers = mutableListOf<Header>()
            val headersContainer = roeMovie.select(".info")

            headersContainer.select("p[class!=star-show]:has(span:not([class=genre])):not(:has(a))")
                    .mapTo(headers) {
                        val split = it.text().split(":")
                        Header(split.first(), split.getOrNull(1)?.trim() ?: "", "")
                    } //解析普通信息

            val content = doc.select("[name=description]").attr("content")?.trim() ?: ""
            headers.add(Header("描述", content, ""))

            headersContainer.select("p[class!=star-show]:has(span:not([class=genre])):has(a)")
                    .mapTo(headers) {
                        val split = it.text().split(":")
                        Header(split.first(), split.getOrNull(1)?.trim()
                                ?: "", it.select("p a").attr("href"))
                    }//解析附带跳转信息

            val geneses = headersContainer.select(".genre:has(a[href*=genre])").map {
                Genre(it.text(), it.select("a").attr("href"))
            }//解析分类


            val actresses = doc.select("#avatar-waterfall .avatar-box").map {
                ActressInfo(it.text(), it.select("img").attr("src"), it.attr("href"))
            }

            val samples = doc.select("#sample-waterfall .sample-box").map {
                val thumb = it.select("img").attr("src")
                val image = it.attr("href")
                ImageSample(it.select("img").attr("title"), thumb, if (TextUtils.isEmpty(image)) thumb else image)
            }

            val relatedMovies = doc.select("#related-waterfall .movie-box").map {
                val url = it.attr("href")
                Movie( it.attr("title"), it.select("img").attr("src"), url.split("/").last(), "", url)
            }

            return MovieDetail(title, content, cover, headers, geneses, actresses, samples, relatedMovies).apply {
                KLog.d("end parseDetails $this")
            }
        }

        fun parseMagnets(doc: Element): List<Magnet> {
            return doc.select("#magnet-table tr:has(a)").map {
                Magnet(it.select("td").getOrNull(0)?.text() ?: "",
                        it.select("td").getOrNull(1)?.text() ?: "",
                        it.select("td").getOrNull(2)?.text() ?: "",
                        it.select("a").attr("href")
//                        it.select("a[class*=btn]").map { it.text() }
                )
            }
        }
    }

}


interface IAttr : Serializable


data class Header(val name: String, val value: String, override val link: String) : ILink {
    @Transient
    override var categoryId: Int = LinkCategory.id ?: 10
}

data class Genre(val name: String, override val link: String) : ILink {
    @Transient
    override var categoryId: Int = LinkCategory.id ?: 10
}

data class ActressInfo(val name: String, val avatar: String, override val link: String, var tag: String? = null) : ILink {

    @Transient
    override var categoryId: Int = ActressCategory.id ?: 2

    companion object {

        fun parseActressAttrs(doc: Document): ActressAttrs {
            val frame = doc.select(".avatar-box")
            val photo = frame.select("img")
            val attrs = frame.select("p").map { it.text() }
            return ActressAttrs(photo.attr("title"), photo.attr("src"), attrs)
        }

        fun parseActressList(doc: Document): List<ActressInfo> {
            return doc.select(".avatar-box")?.map {
                val img = it.select("img")
                ActressInfo(img.attr("title"), img.attr("src"), it.attr("href"), it.select("button").text())
            } ?: emptyList()
        }
    }

    override fun toString() = "ActressInfo(name='$name', avatar='$avatar', link='$link', tag=$tag  categoryId $categoryId) "

}

data class Magnet(val name: String, val size: String, val date: String, override val link: String) : ILink {
    @Transient
    override var categoryId: Int = LinkCategory.id ?: 10
}

data class ImageSample(val title: String, val thumb: String, val image: String)

data class ActressAttrs(val title: String, val imageUrl: String, val info: List<String>) : IAttr


fun MovieDetail.checkUrl(host: String): MovieDetail {
    val nHeader = if (this.headers.any { it.link.urlHost != host }) {
        headers.map {
            it.copy(link = it.link.replace(it.link.urlHost, host))
        }
    } else return this
    val nGenres = if (this.genres.any { it.link.urlHost != host }) {
        genres.map {
            it.copy(link = it.link.replace(it.link.urlHost, host))
        }
    } else return this
    val nActress = if (this.actress.any { it.link.urlHost != host }) {
        actress.map {
            it.copy(link = it.link.replace(it.link.urlHost, host))
        }
    } else return this
    val nRelatedMovies = if (this.relatedMovies.any { it.link.urlHost != host }) {
        relatedMovies.map {
            it.copy(link = it.link.replace(it.link.urlHost, host))
        }
    } else return this
    return this.copy(headers = nHeader, genres = nGenres, actress = nActress, relatedMovies = nRelatedMovies)
}