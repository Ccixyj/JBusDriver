package me.jbusdriver.mvp.bean

import android.text.TextUtils
import me.jbusdriver.http.JAVBusService
import org.jsoup.nodes.Document


/**
 *movie detail
 */
fun parseMovieDetails(doc: Document): MovieDetail {
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
            Header(
                split.first(), split.getOrNull(1)?.trim()
                    ?: "", it.select("p a").attr("href")
            )
        }//解析附带跳转信息

    val geneses = headersContainer.select(".genre:has(a[href*=genre])").map {
        Genre(it.text(), it.select("a").attr("href"))
    }//解析分类


    val actresses = doc.select("#avatar-waterfall .avatar-box").map {
        ActressInfo(it.text(), it.select("img").attr("src").wrapImage(), it.attr("href"))
    }

    val samples = doc.select("#sample-waterfall .sample-box").map {
        val thumb = it.select("img").attr("src").wrapImage()
        val image = it.attr("href")
        ImageSample(
            it.select("img").attr("title"),
            thumb,
            if (TextUtils.isEmpty(image)) thumb else image
        )
    }

    val relatedMovies = doc.select("#related-waterfall .movie-box").map {
        val url = it.attr("href")
        Movie(
            it.attr("title"),
            it.select("img").attr("src").wrapImage(),
            url.split("/").last(), "", url
        )
    }

    return MovieDetail(title, content, cover, headers, geneses, actresses, samples, relatedMovies)
}

/**
 * Actress
 */
fun parseActressAttrs(doc: Document): ActressAttrs {
    val frame = doc.select(".avatar-box")
    val photo = frame.select("img")
    val attrs = frame.select("p").map { it.text() }
    return ActressAttrs(
        photo.attr("title"),
        photo.attr("src").wrapImage(), attrs
    )
}

/**
 * Actress
 */
fun parseActressList(doc: Document): List<ActressInfo> {
    return doc.select(".avatar-box")?.map {
        val img = it.select("img")
        ActressInfo(
            img.attr("title"), img.attr("src").wrapImage(),
            it.attr("href"), it.select("button").text()
        )
    } ?: emptyList()
}

fun String.wrapImage() = if (this.startsWith("http")) this else JAVBusService.defaultFastUrl + this