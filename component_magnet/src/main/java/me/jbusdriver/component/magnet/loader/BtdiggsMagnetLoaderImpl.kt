package me.jbusdriver.component.magnet.loader

import me.jbusdriver.component.magnet.bean.Magnet
import org.jsoup.Jsoup

class BtdiggsMagnetLoaderImpl : IMagnetLoader {
    //  key -> page
    private val search = "https://www.btdigg.biz/search/%s-%s.html"

    override var hasNexPage: Boolean = true


    override fun loadMagnets(key: String, page: Int): List<Magnet> {
        val doc = Jsoup.connect(search.format(key, page)).initHeaders().get()
        hasNexPage = doc.select(".page-split :last-child[title]").size > 0
        return doc.select(".list dl").map {
            val href = it.select("dt a")
            val title = href.text()
            val labels = it.select(".attr span")
            Magnet(title, labels.component2().text(), labels.component1().text(),
                    "http:" + href.attr("href"))
        }

    }
}