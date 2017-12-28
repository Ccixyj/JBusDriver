package  me.jbusdriver.ui.data.magnet

import me.jbusdriver.common.KLog
import me.jbusdriver.common.arrayMapof
import me.jbusdriver.mvp.bean.Magnet
import org.jsoup.Jsoup

interface IMagnetLoader {

    /**
     * 是否有下一页
     */
    var hasNexPage: Boolean

    /**
     * 放入后台线程执行
     */
    fun loadMagnets(key: String, page: Int): List<Magnet>

}

const val MagnetFormatPrefix = "magnet:?xt=urn:btih:"

val MagnetLoaders: Map<String, IMagnetLoader> by lazy {
    arrayMapof("BTSOW" to BTSOWMagnetLoaderImpl(), "Btanv" to BtanvMagnetLoaderImpl())
}


class BTSOWMagnetLoaderImpl : IMagnetLoader {
    //  key -> page
    private val search = "http://www.btsows.com/s/%s/%s/"

    override var hasNexPage: Boolean = true

    override fun loadMagnets(key: String, page: Int): List<Magnet> {
        KLog.d("loadMagnets ${search.format(key.toLowerCase(), page)}")
        val doc = Jsoup.connect(search.format(key, page)).get()
        hasNexPage = doc.select(".pagination").text().equals("next", true)
        return doc.select(".btsowlist .row").map {
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

/**
 * http://www.btanv.com/search/%E9%92%B1%E5%AE%9D%E7%BD%91-first-asc-1
 */

class BtanvMagnetLoaderImpl : IMagnetLoader {
    //  key -> page
    private val search = "http://www.btanv.com/search/%s-first-asc-%s"

    override var hasNexPage: Boolean = true

    override fun loadMagnets(key: String, page: Int): List<Magnet> {
        KLog.d("loadMagnets ${search.format(key.toLowerCase(), page)}")
        val doc = Jsoup.connect(search.format(key, page)).get()
        hasNexPage = (doc.select(".bottom-pager").firstOrNull()?.children()?.size ?: 0) > 0
        return doc.select("#content .search-item").map {
            //it.log()
            val bars = it.select(".item-bar").firstOrNull()?.children()?.map { if (it.tagName() == "a") it.attr("href") else it.text() } ?: emptyList<String>()
            Magnet(it.select(".item-title").text(), bars.find { it.contains("文件大小") } ?: "未知",
                    bars.find { it.contains("收录时间") } ?: "未知", bars.find { it.contains(MagnetFormatPrefix) } ?: "")
        }
    }
}