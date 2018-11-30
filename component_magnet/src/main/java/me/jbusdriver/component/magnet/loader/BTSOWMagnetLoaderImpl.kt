package me.jbusdriver.component.magnet.loader

import me.jbusdriver.component.magnet.bean.Magnet
import me.jbusdriver.component.magnet.loader.IMagnetLoader.Companion.MagnetFormatPrefix
import org.jsoup.Jsoup

class BTSOWMagnetLoaderImpl : IMagnetLoader {
    //  key -> page
    private val search = "http://www.btaaa.com/search/%s_%s.html"

    override var hasNexPage: Boolean = true

    override fun loadMagnets(key: String, page: Int): List<Magnet> {
        val url = search.format(key.trim(), page)
        val doc = Jsoup.connect(url).initHeaders().get()
        val dataNodes = doc.select(".btsowlist .row")
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
            Magnet(hrefNode.text(), size, date, MagnetFormatPrefix + hash)
        }
    }
}