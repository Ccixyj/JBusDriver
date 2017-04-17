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
import com.cfzx.utils.CacheLoader
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.activity_movie_detail.*
import kotlinx.android.synthetic.main.content_movie_detail.*
import me.jbusdriver.common.AppBaseActivity
import me.jbusdriver.common.AppContext
import me.jbusdriver.common.KLog
import me.jbusdriver.common.fromJson
import me.jbusdriver.mvp.MovieDetailContract
import me.jbusdriver.mvp.bean.Magnet
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.bean.MovieDetail
import me.jbusdriver.mvp.bean.detailSaveKey
import me.jbusdriver.mvp.presenter.MovieDetailPresenterImpl
import org.jsoup.Jsoup
import java.text.SimpleDateFormat


class MovieDetailActivity : AppBaseActivity<MovieDetailContract.MovieDetailPresenter, MovieDetailContract.MovieDetailView>(), MovieDetailContract.MovieDetailView {


    var detail: MovieDetail? = null

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
        toolbar.title = movie.code + " " + movie.title
        /*if (detailMovieFromisk == null)
            init()*/
        sr_refresh_detail?.setColorSchemeResources(R.color.colorPrimary,R.color.colorPrimaryDark,R.color.colorPrimaryLight)
        sr_refresh_detail?.setOnRefreshListener { mBasePresenter?.onRefresh() }
    }

    override fun doStart() {
        super.doStart()
        //mBasePresenter初始化完毕后再加载
        if (!hasMagnet) {
            //has disk cache ?
            CacheLoader.acache.getAsString(movie.detailSaveKey + "_magnet")?.let {
                mBasePresenter?.loadMagnets(Jsoup.parse(it))
            } ?: initMagnetLoad()
        }
    }

    override fun initMagnetLoad() {
        KLog.d("load url : ${movie.detailUrl}")
        val mWebView = findViewById(R.id.webview) as WebView
        mWebView.settings.javaScriptEnabled = true
        mWebView.addJavascriptInterface(JavascriptHandler(movie.detailSaveKey + "_magnet"), "handler")
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
    }

    override fun createPresenter() = MovieDetailPresenterImpl()
    override val layoutId = R.layout.activity_movie_detail

    override val movie: Movie by lazy { intent.extras?.getSerializable("movie") as? Movie ?: error("need movie info") }
    //详情不怎么变化,所以直接缓存到disk
    override val detailMovieFromDisk: MovieDetail? by lazy {
        CacheLoader.acache.getAsString(movie.detailSaveKey)?.let { AppContext.gson.fromJson<MovieDetail>(it) }
    }
    override val hasMagnet: Boolean
        get() = (detailMovieFromDisk?.magnets?.isNotEmpty() ?: false) && isDateBeforeNow(movie.date)

    override fun dismissLoading() {
        super.dismissLoading()
        sr_refresh_detail?.post { sr_refresh_detail?.isRefreshing = false }
    }

    override fun <T> showContent(data: T?) {
        if (data is MovieDetail) {
            detail = data
            text.text = data.toString()
        }
    }

    override fun loadMagnet(t: List<Magnet>) {
        text.text = t.toString()
        detail?.apply {
            magnets.clear()
            magnets.addAll(t)
            CacheLoader.cacheDisk(movie.detailSaveKey to this) //重新缓存
            CacheLoader.acache.remove(movie.detailSaveKey + "_magnet") //删除缓存
        }
    }

    /*===========================other===================================*/
    companion object {
        fun start(current: Activity, movie: Movie) {
            current.startActivity(Intent(current, MovieDetailActivity::class.java).apply {
                putExtra("movie", movie)
            })
        }

        fun isDateBeforeNow(time1: String): Boolean {
            try {//如果想比较日期则写成"yyyy-MM-dd"就可以了
                val sdf = SimpleDateFormat("yyyy-MM-dd")
                //将字符串形式的时间转化为Date类型的时间
                val a = sdf.parse(time1)
                //Date类的一个方法，如果a早于b返回true，否则返回false
                return a.before(java.util.Date())
            } catch(e: Exception) {
                return false
            }
        }
    }

    inner class JavascriptHandler(val magnetKey: String) {
        @JavascriptInterface
        open fun getContent(htmlContent: String) {
            //save disk for ever
            Jsoup.parse(htmlContent).select("#magnet-table").first()?.let {
                table ->
                KLog.i("magnetKey :$magnetKey ,table : $table")
                CacheLoader.cacheDisk(magnetKey to table.toString())
                mBasePresenter?.loadMagnets(table)
            }

        }
    }


}
