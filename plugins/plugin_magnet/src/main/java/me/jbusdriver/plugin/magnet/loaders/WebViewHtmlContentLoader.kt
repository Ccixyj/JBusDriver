package me.jbusdriver.plugin.magnet.loaders

import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.*
import android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
import me.jbusdriver.plugin.magnet.BuildConfig
import me.jbusdriver.plugin.magnet.app.instance
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


private const val TAG = "WebContentLoader"

class WebViewHtmlContentLoader {
    private val webView by lazy { WebView(instance) }


    fun startLoad(url: String): String {
        val countDownLatch = CountDownLatch(1)
        val provider = HtmlContentProvider(countDownLatch)
        mainH.post {
            webView.settings?.javaScriptEnabled = true
            webView.settings?.allowContentAccess = true
            webView.settings?.allowFileAccess = true
            webView.settings?.allowUniversalAccessFromFileURLs = true
            webView.settings?.databaseEnabled = true
            webView.settings?.domStorageEnabled = true
            webView.settings?.setAppCacheEnabled(true)
            webView.settings?.mixedContentMode = MIXED_CONTENT_ALWAYS_ALLOW
            webView.settings?.cacheMode = WebSettings.LOAD_DEFAULT
            webView.stopLoading()

            webView.webViewClient = HtmlContentClient(countDownLatch)
            webView.webChromeClient = HtmlLoaderChromeClient(countDownLatch)

            webView.addJavascriptInterface(provider, "html_content")

            webView.loadUrl(url)

        }
        val time = if (BuildConfig.DEBUG) {
            100000L
        } else {
            30L
        }
        return try {
            countDownLatch.await(time, TimeUnit.SECONDS)
            provider.htmlContent
        } catch (e: Exception) {
            ""
        } finally {
            stopLoad()
        }
    }


    fun stopLoad() {
        mainH.post {
            webView.stopLoading()
            webView.destroy()
        }
    }

    companion object {
        private const val JS = """
  javascript: (function(){ var m  = document.querySelector("#magnet-table"); console.log(m && m.childElementCount > 1); if (m && m.childElementCount > 1) { javascript:window.html_content.getSource(m.outerHTML); } })()
"""


        val mainH = Handler(Looper.getMainLooper())
    }

    class HtmlContentProvider(private val countDownLatch: CountDownLatch) {

        var htmlContent: String = ""

        @JavascriptInterface
        fun getSource(html: String) {
            Log.e(TAG, "getSource: ${html.length}")
            if (html.isNotBlank()) {
                htmlContent = "<html>$html</html>"
                countDownLatch.countDown()
            }
        }
    }

    class HtmlLoaderChromeClient(private val countDownLatch: CountDownLatch) :
        android.webkit.WebChromeClient() {

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            Log.e(TAG, "onProgressChanged: $newProgress ... ")
            if (countDownLatch.count > 0 && newProgress >= 70) {
                Log.e(TAG, "onProgressChanged: getSource $JS")
                view?.loadUrl(JS)
            }
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            Log.e(TAG, "onReceivedTitle: $title ")
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            Log.e(TAG, "onConsoleMessage: ${consoleMessage?.message()}")
            return super.onConsoleMessage(consoleMessage)
        }

    }

    class HtmlContentClient(private val countDownLatch: CountDownLatch) : WebViewClient() {

        override fun onReceivedError(
            view: WebView?,
            errorCode: Int,
            description: String?,
            failingUrl: String?
        ) {
            Log.e(TAG, "onReceivedError: $errorCode $description $failingUrl")
            super.onReceivedError(view, errorCode, description, failingUrl)
        }


        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            Log.e(TAG, "onPageStarted: $url")
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            Log.e(TAG, "onPageFinished: $view , $url")
            if (countDownLatch.count > 0) {
                Log.e(TAG, "onProgressChanged: getSource")
                view?.loadUrl(JS)
            }
            super.onPageFinished(view, url)
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            Log.e(TAG, "shouldInterceptRequest ${request?.url}")
            if (request?.url?.path?.contains("jpg|png|gif".toRegex() )== true) {
                Log.e(TAG, "shouldInterceptRequest NONE")
                return null
            }
            return super.shouldInterceptRequest(view, request)
        }

    }


}