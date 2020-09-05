package me.jbusdriver.plugin.magnet.loaders

import android.util.Log
import me.jbusdriver.plugin.magnet.IMagnetLoader
import org.json.JSONObject

class BTBCMagnetLoaderImpl : IMagnetLoader {

    private val searchUrl = "http://www.btbaocai8.pw/search/%s/?c=&s=create_time&p=%s"

    private val Tag = "MagnetLoader:BtBC"
    override var hasNexPage: Boolean = false


    override fun loadMagnets(key: String, page: Int): List<JSONObject> {
        return try {
            val url = searchUrl.format(EncodeHelper.utf8Encode(key), page).trim()
            val doc = IMagnetLoader.safeJsoupGet(url) ?: return emptyList()

            Log.d(Tag, "search :$url")
            hasNexPage = !doc.select(".pagination  li").last().hasClass("disabled")
            return doc.select(".x-item").map { item ->
                val titleDom = item.select(".title")
                val title = titleDom.attr("title")
                val hash = titleDom.attr("href")
                val link = if (hash.startsWith("/hash/")) {
                    IMagnetLoader.MagnetFormatPrefix + hash.removePrefix("/hash/")
                } else "http://www.cilibc.com$hash"
                val infos = item.select(".tail").text().split(" ")
                val split = infos.size / 2
                JSONObject().apply {
                    put("name", title)
                    put("size", infos.take(2).joinToString(" "))
                    put("date", infos.takeLast(infos.size - split).joinToString(" "))
                    put("link", link)
                }
            }

        } catch (e: Exception) {
            emptyList()
        }


    }

    override fun fetchMagnetLink(url: String): String {
        return IMagnetLoader.safeJsoupGet(url)?.select(".row")
            ?.firstOrNull { it.text().contains("磁力链接") }
            ?.select(".value a")?.mapNotNull { it.attr("href") }
            ?.firstOrNull { it.startsWith(IMagnetLoader.MagnetFormatPrefix) }
            ?: url
    }

}
