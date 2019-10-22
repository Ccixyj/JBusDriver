package me.jbusdriver.plugin.magnet.loaders

import me.jbusdriver.plugin.magnet.IMagnetLoader
import me.jbusdriver.plugin.magnet.IMagnetLoader.Companion.MagnetFormatPrefix
import org.json.JSONObject

class BTCherryMagnetLoaderImpl : IMagnetLoader {
    private val search = "https://www.btcherries.space/search/%s-%s.html"

    private val TAG = "MagnetLoader:btcherries"

    override var hasNexPage = false

    override fun loadMagnets(key: String, page: Int): List<JSONObject> {
        val url = search.format(key, page)
        val doc = IMagnetLoader.safeJsoupGet(url) ?: return emptyList()
        hasNexPage =
            !doc.select(".bottom-pager").lastOrNull()?.children()?.lastOrNull()?.attr("href").isNullOrBlank()
        val items = doc.select("#content .search-item")

        return items.map { item ->
            val bar = item.select(".item-bar")
            val allAttrs = bar.select("span")
            val split = allAttrs.size / 2
            val splitAttrs = allAttrs.take(split)
            val link = item.select("a").map {
                it.attr("href").trim()
            }.firstOrNull { it.endsWith(".html") } ?: ""


            val magnet = if (link.startsWith("/magnet")) {
                //A1AAE34799DECB96CFDD779BBBDF7D19809162EF
                //66A9102AFFFD40F089C95CAE8490CF50F825977E
                val tag = ".+/(\\w{40}).html".toRegex().findAll(link).firstOrNull()
                    ?.groupValues?.component2().orEmpty()
                if (tag.isNotBlank() && !tag.startsWith(MagnetFormatPrefix)) {
                    MagnetFormatPrefix + tag
                } else ""
            } else ""

            JSONObject().apply {
                put("name", item.select(".item-title").text().orEmpty())
                put("size", splitAttrs.joinToString("  ") { it.text() })
                put("date", (allAttrs - splitAttrs).joinToString("  ") { it.text() })
                put(
                    "link", magnet
                )
            }
        }
    }
}