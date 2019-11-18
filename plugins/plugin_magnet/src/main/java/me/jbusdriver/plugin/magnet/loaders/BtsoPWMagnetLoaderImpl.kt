package me.jbusdriver.plugin.magnet.loaders

import me.jbusdriver.plugin.magnet.IMagnetLoader
import me.jbusdriver.plugin.magnet.IMagnetLoader.Companion.MagnetFormatPrefix
import me.jbusdriver.plugin.magnet.IMagnetLoader.Companion.safeJsoupGet
import org.json.JSONObject

/**
 * 当前无法使用
 */
class BtsoPWMagnetLoaderImpl : IMagnetLoader {
    //  key -> page https://btos.pw/search/ebod/page/2
    private val search = "https://btos.pw/search/%s/page/%s"

    override var hasNexPage: Boolean = true

    override fun loadMagnets(key: String, page: Int): List<JSONObject> {
        val doc = safeJsoupGet(search.format(key.trim(), page)) ?: return emptyList()
        hasNexPage = doc.select(".pagination [name=nextpage]").isNotEmpty()
        return doc.select(".data-list [class=row]").map {
            val labels = it.children().map { it.text() }.takeLast(2)
            val href = it.select("a")
            val hash = href.attr("href").split("/").last()
            JSONObject().apply {
                put("name", href.attr("title"))
                put("size", labels.first())
                put("date", labels.last())
                put("link", MagnetFormatPrefix + hash)
            }

        }

    }
}