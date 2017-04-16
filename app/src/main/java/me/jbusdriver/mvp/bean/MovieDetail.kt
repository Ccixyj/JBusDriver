package me.jbusdriver.mvp.bean

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
)

interface ILink {
    val link: String
}

data class Header(val name: String, val value: String, override val link: String) : ILink
data class Genre(val name: String, override val link: String) : ILink
data class ActressInfo(val name: String, val avatar: String, override val link: String) : ILink
data class Magnet(val name: String, val size: String, val date: String, override val link: String, val tag: List<String> = listOf()) : ILink
data class ImageSample(val title: String, val thumb: String, val image: String)