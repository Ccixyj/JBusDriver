package me.jbusdriver.mvp.bean

import me.jbusdriver.common.KLog
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Created by Administrator on 2017/4/16.
 */

data class MovieDetail(val title: String,
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
                       val images: List<ImageSample>, //截圖
                       val relatedMovies: List<Movie>, //推薦
                       val magnets: MutableList<Magnet> = mutableListOf() //磁力链接
) {
    companion object {
        fun parseDetails(doc: Document): MovieDetail {
            KLog.d("start parseDetails ")
            val title = doc.select(".container h3").text()
            val roeMovie = doc.select("[class=row movie]")
            val cover = roeMovie.select(".bigImage").attr("href")
            val headers = mutableListOf<Header>()
            val headersContainer = roeMovie.select(".info")

            headersContainer.select("p[class!=star-show]:has(span:not([class=genre])):not(:has(a))")
                    .mapTo(headers) {
                        val split = it.text().split(":")
                        Header(split.first(), split.getOrNull(1) ?: "", "")
                    } //解析普通信息

            headersContainer.select("p[class!=star-show]:has(span:not([class=genre])):has(a)")
                    .mapTo(headers) {
                        val split = it.text().split(":")
                        Header(split.first(), split.getOrNull(1) ?: "", it.select("p a").attr("href"))
                    }//解析附带跳转信息

            val generes = headersContainer.select(".genre:has(a[href*=genre])").map {
                Genre(it.text(), it.select("a").attr("href"))
            }//解析分类


            val actresses = doc.select("#avatar-waterfall .avatar-box").map {
                ActressInfo(it.text(), it.select("img").attr("src"), it.attr("href"))
            }

            val samples = doc.select("#sample-waterfall .sample-box").map {
                ImageSample(it.select("img").attr("title"), it.select("img").attr("src"), it.attr("href"))
            }

            val relatedMovies = doc.select("#related-waterfall .movie-box").map {
                Movie(it.attr("title"), it.select("img").attr("src"), "", "", it.attr("href"))
            }
            KLog.d("end parseDetails ")
            return MovieDetail(title, cover, headers, generes, actresses, samples, relatedMovies)
        }

        fun parseMagnets(doc: Element): List<Magnet> {
            return doc.select("#magnet-table tr:has(a)").map {
                Magnet(it.select("td").getOrNull(0)?.text() ?: "",
                        it.select("td").getOrNull(1)?.text() ?: "",
                        it.select("td").getOrNull(2)?.text() ?: "",
                        it.select("a").attr("href"),
                        it.select("a[class*=btn]").map { it.text() }
                )
            }
        }
    }

}

interface ILink {
    val link: String

}

data class Header(val name: String, val value: String, override val link: String) : ILink
data class Genre(val name: String, override val link: String) : ILink
data class ActressInfo(val name: String, val avatar: String, override val link: String) : ILink
data class Magnet(val name: String, val size: String, val date: String, override val link: String, val tag: List<String> = listOf()) : ILink
data class ImageSample(val title: String, val thumb: String, val image: String)

