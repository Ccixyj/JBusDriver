package me.jbusdriver.ui.data

import android.content.Context
import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.PublishSubject
import me.jbusdriver.common.KLog

/**
 * Created by Administrator on 2017/4/16.
 */
class HtmlContentLoader(val context: Context, url: String) {

    val publish = PublishSubject.create<String>()

    init {
        KLog.d("load url : $url")
        val mWebView = WebView(context)
        mWebView.settings.javaScriptEnabled = true
        mWebView.addJavascriptInterface(JavascriptHandler(publish), "handler")
        mWebView.loadUrl(url)
        mWebView.setWebViewClient(object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                view.loadUrl("javascript:window.handler.getContent(document.body.innerHTML);");
                super.onPageFinished(view, url);
            }

            override fun onReceivedError(view: WebView, errorCode: Int,
                                         description: String, failingUrl: String) {
                super.onReceivedError(view, errorCode, description, failingUrl)
            }

        })
    }

    class JavascriptHandler(val publishSubject: PublishSubject<String>) {
        @JavascriptInterface
        open fun getContent(htmlContent: String) {
            KLog.i("html content:" + htmlContent)
            publishSubject.onNext(htmlContent)
            publishSubject.onComplete()
        }
    }

    fun html2Flowable() = publish.toFlowable(BackpressureStrategy.DROP)!!
}