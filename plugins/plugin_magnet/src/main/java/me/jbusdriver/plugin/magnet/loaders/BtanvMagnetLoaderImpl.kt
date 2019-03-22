package me.jbusdriver.plugin.magnet.loaders

import android.util.Log
import me.jbusdriver.plugin.magnet.IMagnetLoader
import me.jbusdriver.plugin.magnet.IMagnetLoader.Companion.MagnetFormatPrefix
import me.jbusdriver.plugin.magnet.IMagnetLoader.Companion.safeJsoupGet
import me.jbusdriver.plugin.magnet.initHeaders
import org.json.JSONObject
import org.jsoup.Jsoup

class BtanvMagnetLoaderImpl : IMagnetLoader {
    private val Tag = "MagnetLoader:Btanv"
    private val search = "http://www.bteat.com/search/%s-first-asc-%s"
    override var hasNexPage: Boolean =  false

    override fun loadMagnets(key: String, page: Int): List<JSONObject> {
        val url = search.format(EncodeHelper.utf8Encode(key.trim()), page)
        Log.i(Tag, "load url : $url")
        val doc = safeJsoupGet(url) ?: return emptyList()

        hasNexPage = !doc.select(".bottom-pager").firstOrNull()?.children()?.lastOrNull()?.attr("href").isNullOrBlank()
        Log.i(Tag, "hasNexPage : $hasNexPage")
        return doc.select("#content .search-item").map { ele->
            //it.log()
            val bars =
                ele.select(".item-bar").firstOrNull()?.children()?.map { if (it.tagName() == "a") it.attr("href") else it.text() }
                    ?: emptyList<String>()
            JSONObject().apply {
                put("name", ele.select(".item-title").text())
                put("size", bars.find { it.contains("大小") } ?: "未知")
                put("date", bars.find { it.contains("时间") } ?: "未知")
                put("link", bars.find { it.contains(MagnetFormatPrefix) })
            }
        }
    }
}