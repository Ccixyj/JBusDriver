package me.jbusdriver.plugin.magnet.loaders

import android.util.Log
import me.jbusdriver.plugin.magnet.IMagnetLoader
import me.jbusdriver.plugin.magnet.IMagnetLoader.Companion.MagnetFormatPrefix
import me.jbusdriver.plugin.magnet.IMagnetLoader.Companion.safeJsoupGet
import org.json.JSONObject
import java.util.regex.Pattern

class BtAntMagnetLoaderImpl : IMagnetLoader {
    private val Tag = "Btanv"
    private val search = "http://www.eclzz.life/s/%s_rel_%s.html"
    override var hasNexPage: Boolean = false

    private val hashRegex = Pattern.compile("detail/(.+)\\.html")

    override fun loadMagnets(key: String, page: Int): List<JSONObject> {
        val url = search.format(key.replace(" ",""), page)
        Log.i(Tag, "load url : $url")
        val doc = safeJsoupGet(url) ?: return emptyList()

        hasNexPage = doc.select(".pagination").last()?.hasClass("active")?.not() ?: false
        Log.i(Tag, "hasNexPage : $hasNexPage")
        return doc.select("#content .search-item").map { ele ->
            Log.i(Tag, "find item : $ele")
            val bars =
                ele.select(".item-bar").firstOrNull()?.children()?.map {
                    if (it.tagName() == "a") it.attr(
                        "href"
                    ) else it.text()
                } ?: emptyList<String>()

            val hashSource = ele.select(".item-title a").attr("href")
            val hash = hashRegex.matcher(hashSource).let {
                it.find()
                it.group(1).orEmpty()
            }
            val link = IMagnetLoader.MagnetFormatPrefix + hash
            JSONObject().apply {
                put("name", ele.select(".item-title").text())
                put("size", bars.find { it.contains("大小") } ?: "未知")
                put("date", bars.find { it.contains("时间") } ?: "未知")
                put("link", link)
            }
        }
    }
}