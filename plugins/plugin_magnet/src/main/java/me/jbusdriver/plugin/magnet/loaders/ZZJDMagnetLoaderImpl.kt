package me.jbusdriver.plugin.magnet.loaders

import me.jbusdriver.plugin.magnet.IMagnetLoader
import org.json.JSONObject

class ZZJDMagnetLoaderImpl : IMagnetLoader {

    private val search = "http://zhongzijidi.xyz/list/%s/%s/0/0/"
    private val TAG = "MagnetLoader:ZZJD"

    override var hasNexPage = false


    override fun loadMagnets(key: String, page: Int): List<JSONObject> {
        val url = search.format(key, page)
        val doc = IMagnetLoader.safeJsoupGet(url) ?: return emptyList()
        hasNexPage = !doc.select(".pages").lastOrNull()?.children()?.lastOrNull()?.attr("href").isNullOrBlank()
        val items = doc.select(".list-area .item")

        return items.map { item ->
            val allAttrs = item.select(".attr span")
            val links = allAttrs.takeLast(2)
            val attrs = allAttrs.take((allAttrs.size - 2).coerceAtLeast(2))
            val split = attrs.size / 2


            JSONObject().apply {
                put("name", item.child(0).text().orEmpty())
                put("size", attrs.take(split).joinToString("  ") { it.text() })
                put("date", attrs.takeLast(attrs.size - split).joinToString("  ") { it.text() })
                put(
                    "link", IMagnetLoader.MagnetFormatPrefix + links.firstOrNull()?.select("a")?.attr("href")
                        ?.substringBefore(".html")?.substringAfterLast("/").orEmpty()
                )
            }
        }
    }
}