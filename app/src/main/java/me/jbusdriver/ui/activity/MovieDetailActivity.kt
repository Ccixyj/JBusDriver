package me.jbusdriver.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.cfzx.utils.CacheLoader
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.activity_movie_detail.*
import kotlinx.android.synthetic.main.content_movie_detail.*
import me.jbusdriver.common.*
import me.jbusdriver.mvp.MovieDetailContract
import me.jbusdriver.mvp.bean.Magnet
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.bean.MovieDetail
import me.jbusdriver.mvp.bean.detailSaveKey
import me.jbusdriver.mvp.presenter.MovieDetailPresenterImpl
import me.jbusdriver.ui.holder.*
import org.jsoup.Jsoup


class MovieDetailActivity : AppBaseActivity<MovieDetailContract.MovieDetailPresenter, MovieDetailContract.MovieDetailView>(), MovieDetailContract.MovieDetailView {


    private val headHolder by lazy { HeaderHolder(this, movie.type) }
    private val sampleHolder by lazy { ImageSampleHolder(this) }
    private val actressHolder by lazy { ActressListHolder(this, movie.type) }
    private val genreHolder by lazy { GenresHolder(this, movie.type) }
    private val relativeMovieHolder by lazy { RelativeMovieHolder(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            mBasePresenter?.onRefresh()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = movie.code + " " + movie.title

        initWidget()
    }

    private fun initWidget() {
        ll_movie_detail.addView(headHolder.view)
        ll_movie_detail.addView(sampleHolder.view)
        ll_movie_detail.addView(actressHolder.view)
        ll_movie_detail.addView(genreHolder.view)
        ll_movie_detail.addView(relativeMovieHolder.view)
    }

    override fun doStart() {
        super.doStart()
        //mBasePresenter初始化完毕后再加载
        //has disk cache ?
        //  firstLoadMagnet()
    }

    override fun onDestroy() {
        super.onDestroy()
        headHolder.release()
        sampleHolder.release()
        actressHolder.release()
        genreHolder.release()
        relativeMovieHolder.release()
    }

    private fun firstLoadMagnet() {
        CacheLoader.acache.getAsString(movie.detailSaveKey + "_magnet")?.let {
            mBasePresenter?.loadMagnets(Jsoup.parse(it))
        } ?: initMagnetLoad()
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
                view.loadUrl("javascript:window.handler.getContent(document.body.innerHTML);")
                super.onPageFinished(view, url)
            }

            override fun onReceivedError(view: WebView, errorCode: Int,
                                         description: String, failingUrl: String) {
                super.onReceivedError(view, errorCode, description, failingUrl)
            }

        })
    }

    override fun createPresenter() = MovieDetailPresenterImpl()
    override val layoutId = R.layout.activity_movie_detail

    override val movie: Movie by lazy {
        intent.extras?.getSerializable(C.BundleKey.Key_1) as? Movie ?: error("need movie info")
    }
    //详情不怎么变化,所以直接缓存到disk
    override val detailMovieFromDisk: MovieDetail? by lazy {
        CacheLoader.acache.getAsString(movie.detailSaveKey)?.let { AppContext.gson.fromJson<MovieDetail>(it) }
    }

    override fun dismissLoading() {
        super.dismissLoading()
        // sr_refresh_detail?.post { sr_refresh_detail?.isRefreshing = false }
    }

    override fun <T> showContent(data: T?) {
        if (data is MovieDetail) {
            //Slide Up Animation
            KLog.d("date : $data")
            //cover fixme
            iv_movie_cover.setOnClickListener { WatchLargeImageActivity.startShow(this, listOf(data.cover)) }
            Glide.with(this).load(data.cover).thumbnail(0.1f).into(GlideDrawableImageViewTarget(iv_movie_cover))
            //animation
            ll_movie_detail.y = ll_movie_detail.y + 120
            ll_movie_detail.alpha = 0f
            ll_movie_detail.animate().translationY(0f).alpha(1f).setDuration(500).start()

            headHolder.init(data.headers)
            sampleHolder.init(data.imageSamples)
            actressHolder.init(data.actress)
            genreHolder.init(data.genres)
            relativeMovieHolder.init(data.relatedMovies)

        }
    }

    override fun addMagnet(t: List<Magnet>) {
        //如果movie含有tag说明有种子了,重新加载
        if (movie.tags.isNotEmpty() && t.isEmpty()) {
            initMagnetLoad()
        }
    }

    /*===========================other===================================*/
    companion object {
        fun start(current: Context, movie: Movie) {
            current.startActivity(Intent(current, MovieDetailActivity::class.java).apply {
                putExtra(C.BundleKey.Key_1, movie)
            })
        }

    }

    inner class JavascriptHandler(val magnetKey: String) {
        @JavascriptInterface
        open fun getContent(htmlContent: String) {
            //save disk for ever
            Jsoup.parse(htmlContent).select("#magnet-table").first()?.let {
                table ->
                KLog.i("magnetKey :$magnetKey ,table : $table")
                CacheLoader.cacheDisk(magnetKey to table.toString(), ACache.TIME_DAY)
                mBasePresenter?.loadMagnets(table)
            }

        }
    }


}
