package me.jbusdriver.plugin.magnet.loaders

import me.jbusdriver.plugin.magnet.IMagnetLoader
import org.json.JSONObject

class BTSearchMagnetLoaderImpl : IMagnetLoader {
    private val search = "https://www.cilisha.xyz/search/%s-%s-time.html"

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
            val split = allAttrs.size / 2
            val splitAttrs =allAttrs.take(split)


            JSONObject().apply {
                put("name", item.select(".item-title").text().orEmpty())
                put("size", splitAttrs.joinToString("  ") { it.text() })
                put("date", (allAttrs - splitAttrs).joinToString("  ") { it.text() })
                put(
                    "link",
                    links.map {
                        it.attr("href").trim()
                    }.firstOrNull { it.startsWith(IMagnetLoader.MagnetFormatPrefix) }.orEmpty()
                )
            }
        }
    }
}