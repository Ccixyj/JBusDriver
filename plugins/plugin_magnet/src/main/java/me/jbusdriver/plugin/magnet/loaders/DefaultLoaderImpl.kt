package me.jbusdriver.plugin.magnet.loaders

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import me.jbusdriver.plugin.magnet.BuildConfig
import me.jbusdriver.plugin.magnet.IMagnetLoader
import me.jbusdriver.plugin.magnet.app.instance
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


private const val TAG = "DefaultLoaderImpl"

@SuppressLint("JavascriptInterface")
class DefaultLoaderImpl : IMagnetLoader {


    private val webView by lazy { WebView(instance) }
    private val mainH = Handler(Looper.getMainLooper())

    /**
     * 默认一页
     */
    override var hasNexPage: Boolean = false

    init {

    }

    override fun loadMagnets(key: String, page: Int): List<JSONObject> {
        require(URLUtil.isHttpUrl(key) || URLUtil.isHttpsUrl(key)) { "需要为网络连接!" }
        require(Looper.getMainLooper() != Looper.myLooper()) { "需要在子线程执行!" }
        val countDownLatch = CountDownLatch(1)
        val provider = HtmlContentProvider(countDownLatch)
        mainH.post {
            Log.d(TAG, "loadMagnets: start $key")
            webView.settings?.javaScriptEnabled = true
            webView.webViewClient = HtmlContentClient()
            webView.addJavascriptInterface(provider, "html_content")
            webView.loadUrl(key)
        }
        val time = if (BuildConfig.DEBUG) {
            100000L
        } else {
            30L
        }
        countDownLatch.await(time, TimeUnit.SECONDS)
        Log.d(TAG, "loadMagnets: ${provider.htmlContent}")
        return Jsoup.parse(provider.htmlContent).select("#magnet-table tr").asSequence()
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

    class HtmlContentProvider(private val countDownLatch: CountDownLatch) {

        var htmlContent: String = ""

        @JavascriptInterface
        fun getSource(html: String) {
            Log.d(TAG, "getSource: $html")
            if (html.isNotBlank()) {
                htmlContent = html
                countDownLatch.countDown()
            }
        }
    }

    class HtmlContentClient : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            Log.d(TAG, "onPageStarted: $url")
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            Log.d(TAG, "onPageFinished: $view , $url")
            view?.loadUrl(
                "javascript:window.html_content.getSource("
                        + "document.getElementsByTagName('html')[0].innerHTML);"
            )
            super.onPageFinished(view, url)
        }
    }

}
