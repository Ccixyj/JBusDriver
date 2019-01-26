package me.jbusdriver.plugin.magnet.loaderImpl


import me.jbusdriver.plugin.magnet.IMagnetLoader
import org.json.JSONObject
import org.jsoup.Jsoup

class CNBtkittyMangetLoaderImpl : IMagnetLoader {

    private val search = "http://cnbtkitty.ws/"

    override var hasNexPage: Boolean = true

    var searcLinkNext = ""

    init {
        val manager = java.net.CookieManager()
        manager.setCookiePolicy(java.net.CookiePolicy.ACCEPT_ALL)
        java.net.CookieHandler.setDefault(manager)
    }


    override fun loadMagnets(key: String, page: Int): List<JSONObject> {
        return try {
            val doc = if (page == 1) {
                Jsoup.connect(search).cookie("bk_lan", "zh-cn").data("keyword", key).initHeaders().post()
            } else {
                if (!searcLinkNext.startsWith("http")) {
                    val schema = search.split("://").last()
                    if (!searcLinkNext.contains(schema)) {
                        searcLinkNext = schema + searcLinkNext
                    }
                    searcLinkNext = "https://" + searcLinkNext.removePrefix(":").removePrefix("/").removePrefix("/")
                }
                Jsoup.connect(searcLinkNext).initHeaders().get()
            }
            val nextPages = doc.select(".pagination strong~a")
            hasNexPage = nextPages.size > 0
            if (hasNexPage) {
                searcLinkNext = nextPages.first().attr("href")
            }


            return doc.select(".content .list-con").map {
                val title = it.select("dt").text().trim().removeSuffix("Hot")
                val ops = it.select(".option span")

                val magent = ops.removeAt(0).select("a").attr("href")
                ops.removeAt(0)
                val splitIndex = ops.size / 2

                JSONObject().apply {
                    put("name", title)
                    put("size", ops.take(splitIndex).joinToString(" ") { it.text() })
                    put("date", ops.takeLast(ops.size - splitIndex).joinToString(" ") { it.text() })
                    put("link", magent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }


    }


    override fun fetchMagnetLink(url: String): String {
        return Jsoup.connect(search + url).get().select(".content .magnet a").attr("href")
    }
}