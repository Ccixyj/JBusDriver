package me.jbusdriver.plugin.magnet.loaders

import android.util.Log
import me.jbusdriver.plugin.magnet.IMagnetLoader
import me.jbusdriver.plugin.magnet.IMagnetLoader.Companion.MagnetFormatPrefix
import org.json.JSONObject

class BTSOWMagnetLoaderImpl : IMagnetLoader {

    private val Tag = "MagnetLoader:BTSOW"

    //  key -> page
    private val search = "https://www.btsow.in/com/%s_%s.html"

    override var hasNexPage: Boolean = false

    override fun loadMagnets(key: String, page: Int): List<JSONObject> {
        val url = search.format(EncodeHelper.utf8Encode(key.trim()), page)
        val doc = IMagnetLoader.safeJsoupGet(url) ?: return emptyList()
        val dataNodes = doc.select(".row")
        Log.i(Tag, "row $dataNodes")
        hasNexPage = (doc.select(".pagination a").lastOrNull()?.attr("href")?.split("/")
            ?.lastOrNull { it.isNotBlank() && it.toIntOrNull() != null }?.toIntOrNull()
            ?: -1) > 0
        return dataNodes.map {
            val hrefNode = it.select("a")
            val childs = it.children()
            val size = childs.getOrNull(1)?.text() ?: "未知"
            val date = childs.getOrNull(2)?.text() ?: "未知"
            val href = hrefNode.attr("href")
            val hash = href.split("/").last()
            JSONObject().apply {
                put("name", hrefNode.text())
                put("size", size)
                put("date", date)
                put("link", MagnetFormatPrefix + hash)
            }

        }
    }
}