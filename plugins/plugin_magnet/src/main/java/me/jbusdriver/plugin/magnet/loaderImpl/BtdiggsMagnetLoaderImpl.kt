package me.jbusdriver.plugin.magnet.loaderImpl
import me.jbusdriver.plugin.magnet.common.bean.Magnet
import me.jbusdriver.plugin.magnet.common.loader.IMagnetLoader
import org.jsoup.Jsoup

class BtdiggsMagnetLoaderImpl : IMagnetLoader {
    //  key -> page
    private val search = "https://www.btdigg.xyz/search/%s/%s/1/0.html"

    override var hasNexPage: Boolean = true


    override fun loadMagnets(key: String, page: Int): List<Magnet> {
        val doc = Jsoup.connect(search.format(encode(key), page)).initHeaders().get()
        hasNexPage = doc.select(".page-split :last-child[title]").size > 0
        return doc.select(".list dl").map {
            val href = it.select("dt a")
            val title = href.text()
            val url = href.attr("href")

            val realUrl = when {
                url.startsWith("www.") -> "https://$url"
                url.startsWith("/magnet") -> {
                    IMagnetLoader.MagnetFormatPrefix + url.removePrefix("/magnet/").removeSuffix(".html")
                }
                else -> "https://www.btdigg.xyz$url"
            }

            val labels = it.select(".attr span")
            Magnet(title, labels.component2().text(), labels.component1().text(), realUrl)
        }

    }

    override fun fetchMagnetLink(url: String): String {
        return (IMagnetLoader.MagnetFormatPrefix + Jsoup.connect(url).get().select(".content .infohash").text().trim())
    }
}