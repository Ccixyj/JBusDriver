package me.jbusdriver.plugin.magnet.loaders

import me.jbusdriver.plugin.magnet.IMagnetLoader
import me.jbusdriver.plugin.magnet.IMagnetLoader.Companion.MagnetFormatPrefix
import org.json.JSONObject
import org.jsoup.Jsoup

class BtanvMagnetLoaderImpl : IMagnetLoader {

    private val search = "https://www.btmae.com/ma/%s/time-%s.html"
    override var hasNexPage: Boolean = true

    override fun loadMagnets(key: String, page: Int): List<JSONObject> {
        val doc = Jsoup.connect(search.format(key.trim(), page)).initHeaders().get()
        hasNexPage = (doc.select(".bottom-pager").firstOrNull()?.children()?.size ?: 0) > 0
        return doc.select("#content .cili-item").map {
            //it.log()
            val bars =
                it.select(".item-bar").firstOrNull()?.children()?.map { if (it.tagName() == "a") it.attr("href") else it.text() }
                    ?: emptyList<String>()
            JSONObject().apply {
                put("name", it.select(".item-title").text())
                put("size", bars.find { it.contains("大小") } ?: "未知")
                put("date", bars.find { it.contains("时间") } ?: "未知")
                put("link", bars.find { it.contains(MagnetFormatPrefix) })
            }
        }
    }
}