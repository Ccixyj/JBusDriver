package me.jbusdriver.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.cfzx.utils.CacheLoader
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.content_movie_detail.*
import me.jbusdriver.common.*
import me.jbusdriver.mvp.MovieDetailContract
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.mvp.presenter.MovieDetailPresenterImpl
import me.jbusdriver.ui.data.GridSpacingItemDecoration
import org.jsoup.Jsoup


class MovieDetailActivity : AppBaseActivity<MovieDetailContract.MovieDetailPresenter, MovieDetailContract.MovieDetailView>(), MovieDetailContract.MovieDetailView {

    val headAdapter = object : BaseQuickAdapter<Header, BaseViewHolder>(R.layout.layout_header_item) {
        override fun convert(helper: BaseViewHolder, item: Header) {
            helper.getView<TextView>(R.id.tv_head_value)?.apply {
                if (!TextUtils.isEmpty(item.link)) {
                    setTextColor(ResourcesCompat.getColor(this@apply.resources, R.color.colorPrimaryDark, null))
                    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                    setOnClickListener {
                        KLog.d("text : ${this.text}")
                    }
                } else {
                    setTextColor(ResourcesCompat.getColor(this@apply.resources, R.color.secondText, null))
                    paintFlags = 0
                    setOnClickListener(null)
                }
            }
            helper.setText(R.id.tv_head_name, item.name)
                    .setText(R.id.tv_head_value, item.value)
        }
    }

    val imageSampleAdapter = object : BaseQuickAdapter<ImageSample, BaseViewHolder>(R.layout.layout_image_sample_item) {
        override fun convert(helper: BaseViewHolder, item: ImageSample) {
            helper.getView<ImageView>(R.id.iv_movie_thumb)?.let {
                Glide.with(this@MovieDetailActivity).load(item.thumb)
                        .fitCenter()
                        .placeholder(R.drawable.ic_child_care_black_24dp)
                        .error(R.drawable.ic_child_care_black_24dp)
                        .into(GlideDrawableImageViewTarget(it))

            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            mBasePresenter?.onRefresh()
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.title = movie.code + " " + movie.title

        initHead()
        initImages()
    }

    private fun initHead() {
        rv_recycle_header.layoutManager = LinearLayoutManager(this)

        rv_recycle_header.adapter = headAdapter
    }

    private fun initImages() {
        val displayMetrics = DisplayMetrics()
        (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displayMetrics)
        val spannCount = when (displayMetrics.widthPixels) {
            in 0..1079 -> 3
            in 1080..1920 -> 4
            else -> 6
        }
        rv_recycle_images.layoutManager = StaggeredGridLayoutManager(spannCount, StaggeredGridLayoutManager.VERTICAL)
        rv_recycle_images.addItemDecoration(GridSpacingItemDecoration(spannCount, dpToPx(6f), false))
        rv_recycle_images.adapter = imageSampleAdapter
    }

    override fun doStart() {
        super.doStart()
        //mBasePresenter初始化完毕后再加载
        //has disk cache ?
      //  firstLoadMagnet()
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

    override fun dismissLoading() {
        super.dismissLoading()
        // sr_refresh_detail?.post { sr_refresh_detail?.isRefreshing = false }
    }

    override fun <T> showContent(data: T?) {
        if (data is MovieDetail) {
            //Slide Up Animation
            KLog.d("date : $data")
            //animation
            ll_movie_detail.y = ll_movie_detail.y + 120
            ll_movie_detail.alpha = 0f
            ll_movie_detail.animate().translationY(0f).alpha(1f).setDuration(500).start()

            //header
            if (data.headers.isEmpty()) tv_movie_head_none_tip.visibility = View.VISIBLE
            else {
                //load header
                headAdapter.setNewData(data.headers)
            }

            //images
            if (data.images.isEmpty()) tv_movie_images_none_tip.visibility = View.VISIBLE
            else {
                //load header
                imageSampleAdapter.setNewData(data.images)
            }
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
        fun start(current: Activity, movie: Movie) {
            current.startActivity(Intent(current, MovieDetailActivity::class.java).apply {
                putExtra("movie", movie)
            })
        }

        fun View.measureIfNotMeasure() {
            if (this.measuredHeight != 0 || this.measuredWidth != 0) return
            this.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
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
