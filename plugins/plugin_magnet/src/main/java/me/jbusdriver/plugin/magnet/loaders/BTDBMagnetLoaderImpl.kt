package me.jbusdriver.plugin.magnet.loaders


import me.jbusdriver.plugin.magnet.IMagnetLoader
import org.json.JSONObject
import org.jsoup.Jsoup

class BTDBMagnetLoaderImpl : IMagnetLoader {
    override var hasNexPage: Boolean = true

    private val search = "https://btdb.to/q/%s/%s"

    override fun loadMagnets(key: String, page: Int): List<JSONObject> {
        val url = search.format(key, page)
        val doc = Jsoup.connect(url).get()

        val nextHref = doc.select(".pagination  a").last() ?: let {
            hasNexPage = false
            return emptyList()
        }
        hasNexPage = !(nextHref.attr("href") == "#" && nextHref.className() == "disabled")

        return doc.select(".search-ret  .search-ret-item").map {
            val title = it.select(".item-title").text()
            val meta = it.select(".item-meta-info")
            val metaInfos = meta.select("span").map { it.text() }
            val link = meta.select(".magnet").attr("href")
            JSONObject().apply {
                put("name", title)
                put("size", metaInfos.getOrNull(0).orEmpty())
                put("date", metaInfos.getOrNull(2).orEmpty())
                put("link", link)
            }
        }
    }
}