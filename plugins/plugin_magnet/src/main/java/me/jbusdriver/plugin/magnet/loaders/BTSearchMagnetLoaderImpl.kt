package me.jbusdriver.plugin.magnet.loaders

import me.jbusdriver.plugin.magnet.IMagnetLoader
import org.json.JSONObject

class BTSearchMagnetLoaderImpl : IMagnetLoader {
    private val search = "https://www.cilisharez.com/search/%s-%s-time.html"

    private val TAG = "MagnetLoader:btsearch"

    override var hasNexPage = false

    override fun loadMagnets(key: String, page: Int): List<JSONObject> {
        val url = search.format(EncodeHelper.str2HexStr(key), page)
        val doc = IMagnetLoader.safeJsoupGet(url) ?: return emptyList()
        hasNexPage = !doc.select(".bottom-pager").lastOrNull()?.children()?.lastOrNull()?.attr("href").isNullOrBlank()
        val items = doc.select("#content .search-item")

        return items.map { item ->
            val bar = item.select(".item-bar")
            val allAttrs = bar.select("span")
            val links = bar.select("a")
            val attrs = allAttrs.take((allAttrs.size - 2).coerceAtLeast(2))
            val split = attrs.size / 2


            JSONObject().apply {
                put("name", item.select(".item-title").text().orEmpty())
                put("size", attrs.take(split).joinToString("  ") { it.text() })
                put("date", attrs.takeLast(attrs.size - split).joinToString("  ") { it.text() })
                put(
                    "link", links.asSequence().first { it.attr("href").trim().startsWith(IMagnetLoader.MagnetFormatPrefix) }           )
            }
        }
    }
}