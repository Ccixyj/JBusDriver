package me.jbusdriver.plugin.magnet.loaders

import android.text.Html
import android.util.Log
import me.jbusdriver.plugin.magnet.IMagnetLoader
import org.json.JSONObject
import java.util.regex.Pattern

class ChaoRenLoaderImpl : IMagnetLoader {

    private val searchUrl = "https://www.chaoren99.xyz/search/page-%s.html?name=%s"

    private val Tag = "ChaoRen"
    override var hasNexPage: Boolean = false

    private val hashRegex = Pattern.compile("hash/(.+)\\.html")

    override fun loadMagnets(key: String, page: Int): List<JSONObject> {
        return try {
            val url = searchUrl.format(page, EncodeHelper.utf8Encode(key)).trim()
            val doc = IMagnetLoader.safeJsoupGet(url) ?: return emptyList()

            Log.d(Tag, "search :$url")
            hasNexPage = !doc.select(".pagination li").last().hasClass("active")
            return doc.select(".container .item").map { item ->
                Log.d(Tag, "find :$item")
                val title = Html.fromHtml(item.select("h4").html())
                val hashSource = item.select("a").first()?.attr("href").orEmpty()
                val hash = hashRegex.matcher(hashSource).let {
                    it.find()
                    it.group(1).orEmpty()
                }
                val link = IMagnetLoader.MagnetFormatPrefix + hash
                Log.d(Tag, "find link :$link")
                val infos = item.select("p").text().split(" ")
                val split = infos.size / 2
                JSONObject().apply {
                    put("name", title)
                    put("size", infos.take(2).joinToString(" "))
                    put("date", infos.takeLast(infos.size - split).joinToString(" "))
                    put("link", link)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }


    }

    override fun fetchMagnetLink(url: String): String {
        return ""
    }

}
