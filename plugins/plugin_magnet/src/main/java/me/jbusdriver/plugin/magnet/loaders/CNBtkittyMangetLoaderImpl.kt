package me.jbusdriver.plugin.magnet.loaders


import android.util.Log
import me.jbusdriver.plugin.magnet.IMagnetLoader
import me.jbusdriver.plugin.magnet.IMagnetLoader.Companion.safeJsoupGet
import me.jbusdriver.plugin.magnet.loaders.EncodeHelper.gzDeflateBase64
import org.json.JSONObject

class CNBtkittyMangetLoaderImpl : IMagnetLoader {

    private val host = "http://btkitty.pet/"
    private val search = "http://btkitty.pet/search/%s/%s/0/0.html"

    override var hasNexPage: Boolean = false

    private val Tag = "MagnetLoader:CNBtkitty"

    init {
        val manager = java.net.CookieManager()
        manager.setCookiePolicy(java.net.CookiePolicy.ACCEPT_ALL)
        java.net.CookieHandler.setDefault(manager)
    }


    override fun loadMagnets(key: String, page: Int): List<JSONObject> {
        return try {
            val url = search.format(gzDeflateBase64(key), page)
            Log.i(Tag, "laod url $url")
            val doc = IMagnetLoader.safeJsoupGet(url) ?: return emptyList()
            val nextPages = doc.select(".pagination strong~a")
            hasNexPage = nextPages.size > 0

            return doc.select(".content .list-con").map {
                val title = it.select("dt").text().trim().removeSuffix("Hot")
                val ops = it.select(".option span")

                val magent = ops.removeAt(0).select("a").attr("href")
                ops.removeAt(0)
                val splitIndex = ops.size / 2

                JSONObject().apply {
                    put("name", title)
                    put("size", ops.take(splitIndex).joinToString(" ") { it.text() })
                    put("date", ops.takeLast(ops.size - splitIndex).joinToString(" ") { it.text() })
                    put("link", magent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(Tag, "throw error $e", e)
            emptyList()

        }


    }


    override fun fetchMagnetLink(url: String): String {
        return safeJsoupGet(host + url)?.select(".content .magnet a")?.attr("href").orEmpty()
    }
}