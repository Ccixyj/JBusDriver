package me.jbusdriver.mvp.bean

import me.jbusdriver.base.urlHost
import me.jbusdriver.db.bean.ActressCategory
import me.jbusdriver.db.bean.LinkCategory
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
)

interface IAttr : Serializable


data class Header(val name: String, val value: String, override val link: String) : ILink {
    @Transient
    override var categoryId: Int = LinkCategory.id ?: 10
}

data class Genre(val name: String, override val link: String) : ILink {
    @Transient
    override var categoryId: Int = LinkCategory.id ?: 10
}

data class ActressInfo(val name: String, val avatar: String, override val link: String, @Transient var tag: String? = null) : ILink {

    @Transient
    override var categoryId: Int = ActressCategory.id ?: 2



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