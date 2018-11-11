package me.jbusdriver.component.magnet.loader

import me.jbusdriver.component.magnet.bean.Magnet
import org.jsoup.Jsoup

class TorrentKittyMangetLoaderImpl : IMagnetLoader {

    private var search = "https://www.torrentkitty.tv/search/%s/%s"

    override var hasNexPage: Boolean = true

    init {
        val manager = java.net.CookieManager()
        manager.setCookiePolicy(java.net.CookiePolicy.ACCEPT_ALL)
        java.net.CookieHandler.setDefault(manager)
    }

    override fun loadMagnets(key: String, page: Int): List<Magnet> {
        val doc = Jsoup.connect(search.format(key, page)).get()
        hasNexPage = (doc.select(".pagination").firstOrNull()?.select(".current~a")?.size ?: 0) > 0
        val mag = doc.select("#archiveResult tr").drop(1)
        return mag.map {
            val link = it.select(".action a[rel=magnet]")
            Magnet(it.select(".name").text(), it.select(".size").text(),
                    it.select(".date").text(), link.attr("href"))
        }

    }
}