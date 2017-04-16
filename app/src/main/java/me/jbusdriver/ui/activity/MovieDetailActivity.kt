package me.jbusdriver.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.widget.Toolbar
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import io.reactivex.android.schedulers.AndroidSchedulers
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.content_movie_detail.*
import me.jbusdriver.common.AppBaseActivity
import me.jbusdriver.common.KLog
import me.jbusdriver.mvp.MovieDetailContract
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.presenter.MovieDetailPresenterImpl


class MovieDetailActivity : AppBaseActivity<MovieDetailContract.MovieDetailPresenter, MovieDetailContract.MovieDetailView>(), MovieDetailContract.MovieDetailView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.title = movie.title


        init()
    }

    fun init() {
        KLog.d("load url : ${movie.detailUrl}")
        val mWebView = findViewById(R.id.webview) as WebView
        mWebView.settings.javaScriptEnabled = true
        mWebView.addJavascriptInterface(JavascriptHandler(), "handler")
        mWebView.loadUrl(movie.detailUrl)
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

    override fun onStart() {
        super.onStart()
        //HtmlContentLoader(viewContext, movie.detailUrl).html2Flowable().subscribeWith(SimpleSubscriber()).addTo(rxManager)
    }

    override fun createPresenter() = MovieDetailPresenterImpl()
    override val layoutId = R.layout.activity_movie_detail

    override val movie: Movie by lazy { intent.extras?.getSerializable("movie") as? Movie ?: error("need movie info") }

    companion object {
        fun start(current: Activity, movie: Movie) {
            current.startActivity(Intent(current, MovieDetailActivity::class.java).apply {
                putExtra("movie", movie)
            })
        }
    }

    override fun <T> showContent(data: T?) {
        text.text = data.toString()
    }


    inner class JavascriptHandler {
        @JavascriptInterface
        open fun getContent(htmlContent: String) {
            KLog.i("html content:" + htmlContent)
            AndroidSchedulers.mainThread().scheduleDirect { showContent(htmlContent) }
        }
    }

}
