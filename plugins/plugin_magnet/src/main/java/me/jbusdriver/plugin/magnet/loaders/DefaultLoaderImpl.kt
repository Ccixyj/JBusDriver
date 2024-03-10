package me.jbusdriver.plugin.magnet.loaders

import android.annotation.SuppressLint
import android.os.Looper
import android.util.Log
import android.webkit.URLUtil
import me.jbusdriver.plugin.magnet.IMagnetLoader
import org.json.JSONObject
import org.jsoup.Jsoup


private const val TAG = "DefaultLoaderImpl"

@SuppressLint("JavascriptInterface")
class DefaultLoaderImpl : IMagnetLoader {




    /**
     * 默认一页
     */
    override var hasNexPage: Boolean = false


    override fun loadMagnets(key: String, page: Int): List<JSONObject> {
        require(URLUtil.isHttpUrl(key) || URLUtil.isHttpsUrl(key)) { "需要为网络连接!" }
        require(Looper.getMainLooper() != Looper.myLooper()) { "需要在子线程执行!" }
        val content = WebViewHtmlContentLoader().startLoad(key)
        Log.e(TAG, "loadMagnets: $content")
        return Jsoup.parse(content).select("#magnet-table tr").asSequence()
            .drop(1).map {
                val contents = it.select("td")
                val link = it.select("a").attr("href").orEmpty()
                JSONObject().apply {
                    put("name", contents.getOrNull(0)?.text().orEmpty())
                    put("size", contents.getOrNull(1)?.text().orEmpty())
                    put("date", contents.getOrNull(2)?.text().orEmpty())
                    put("link", link)
                }
            }.toList()

    }


}
