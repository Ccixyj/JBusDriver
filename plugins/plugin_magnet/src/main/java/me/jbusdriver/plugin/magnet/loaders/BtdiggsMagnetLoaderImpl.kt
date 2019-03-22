package me.jbusdriver.plugin.magnet.loaders

import android.util.Log

import me.jbusdriver.plugin.magnet.IMagnetLoader
import me.jbusdriver.plugin.magnet.IMagnetLoader.Companion.safeJsoupGet
import me.jbusdriver.plugin.magnet.initHeaders
import me.jbusdriver.plugin.magnet.loaders.EncodeHelper.encodeBase64
import org.json.JSONObject
import org.jsoup.Jsoup

class BtdiggsMagnetLoaderImpl : IMagnetLoader {
    //  key -> page
    private val search = "https://www.btdigg.xyz/search/%s/%s/1/0.html"

    override var hasNexPage: Boolean = false
    val TAG = "MagnetLoader:Btdiggs"

    override fun loadMagnets(key: String, page: Int): List<JSONObject> {
        val formatUrl = search.format(encodeBase64(key), page)
        Log.w(TAG, "load url :$formatUrl")
        return try {
            val doc = IMagnetLoader.safeJsoupGet(formatUrl) ?: return emptyList()
            Log.i(TAG, "load doc :${doc.title()}")
            hasNexPage = doc.select(".page-split :last-child[title]").size > 0
            doc.select(".list dl").map {
                val href = it.select("dt a")
                val title = href.text()
                val url = href.attr("href")

                val realUrl = when {
                    url.startsWith("www.") -> "https://$url"
                    url.startsWith("/magnet") -> {
                        IMagnetLoader.MagnetFormatPrefix + url.removePrefix("/magnet/").removeSuffix(".html")
                    }
                    else -> "https://www.btdigg.xyz$url"
                }

                val labels = it.select(".attr span")
                JSONObject().apply {
                    put("name", title)
                    put("size", labels.component2().text())
                    put("date", labels.component1().text())
                    put("link", realUrl)
                }


            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "throw error $e", e)
            emptyList()

        }
    }


    override fun fetchMagnetLink(url: String): String {
        return (IMagnetLoader.MagnetFormatPrefix +     safeJsoupGet(url)?.select(".content .infohash")?.text()?.trim().orEmpty())
    }
}

