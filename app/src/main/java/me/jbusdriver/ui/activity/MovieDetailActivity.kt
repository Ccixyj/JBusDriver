package me.jbusdriver.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.gyf.barlibrary.ImmersionBar
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.activity_movie_detail.*
import kotlinx.android.synthetic.main.content_movie_detail.*
import kotlinx.android.synthetic.main.layout_load_magnet.view.*
import me.jbusdriver.base.GlideApp
import me.jbusdriver.base.KLog
import me.jbusdriver.base.common.AppBaseActivity
import me.jbusdriver.base.common.C
import me.jbusdriver.base.glide.toGlideNoHostUrl
import me.jbusdriver.base.inflate
import me.jbusdriver.base.urlPath
import me.jbusdriver.mvp.MovieDetailContract
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.bean.MovieDetail
import me.jbusdriver.mvp.bean.convertDBItem
import me.jbusdriver.mvp.bean.des
import me.jbusdriver.mvp.model.CollectModel
import me.jbusdriver.mvp.presenter.MovieDetailPresenterImpl
import me.jbusdriver.ui.holder.*


class MovieDetailActivity : AppBaseActivity<MovieDetailContract.MovieDetailPresenter, MovieDetailContract.MovieDetailView>(), MovieDetailContract.MovieDetailView {

    private val statusBarHeight by lazy { ImmersionBar.getActionBarHeight(this) }

    private lateinit var collectMenu: MenuItem
    private lateinit var removeCollectMenu: MenuItem

    private val headHolder by lazy { HeaderHolder(this) }
    private val sampleHolder by lazy { ImageSampleHolder(this) }
    private val actressHolder by lazy { ActressListHolder(this) }
    private val genreHolder by lazy { GenresHolder(this) }
    private val relativeMovieHolder by lazy { RelativeMovieHolder(this) }

    override val url by lazy { intent.getStringExtra(C.BundleKey.Key_1) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {

            mBasePresenter?.onRefresh()

        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = movie?.des
        immersionBar.transparentStatusBar().titleBar(toolbar).statusBarAlpha(0.12f).init()
        initWidget()
        initData()

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_movie_detail, menu)
        collectMenu = menu.findItem(R.id.action_add_movie_collect)
        removeCollectMenu = menu.findItem(R.id.action_remove_movie_collect)
        val saveItem = movie?.convertDBItem() ?: return true
        if (CollectModel.has(saveItem)) {
            collectMenu.isVisible = false
            removeCollectMenu.isVisible = true
        } else {
            collectMenu.isVisible = true
            removeCollectMenu.isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the CENSORED/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        val saveItem = movie?.convertDBItem() ?: return super.onOptionsItemSelected(item)


        when (id) {
            R.id.action_add_movie_collect -> {
                //收藏
                KLog.d("收藏")
                CollectModel.addToCollectForCategory(saveItem) {
                    collectMenu.isVisible = false
                    removeCollectMenu.isVisible = true
                }

            }
            R.id.action_remove_movie_collect -> {
                //取消收藏
                KLog.d("取消收藏")
                if (CollectModel.removeCollect(saveItem)) {
                    collectMenu.isVisible = true
                    removeCollectMenu.isVisible = false
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initWidget() {
        sr_refresh.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimaryLight)
        sr_refresh.setOnRefreshListener {
            //reload
            mBasePresenter?.onRefresh()

        }

        app_bar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, offset -> sr_refresh.isEnabled = Math.abs(offset) <= 1 })


        ll_movie_detail.addView(headHolder.view)
        ll_movie_detail.addView(sampleHolder.view)
        ll_movie_detail.addView(viewContext.inflate(R.layout.layout_load_magnet).apply {
            this.tv_movie_look_magnet.setTextColor(ResourcesCompat.getColor(this@apply.resources, R.color.colorPrimaryDark, null))
            this.tv_movie_look_magnet.paintFlags = this.tv_movie_look_magnet.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener {
                val code = movie?.code?.replace("-", "") ?: url.urlPath
                MagnetPagerListActivity.start(viewContext, code)
            }
        })
        ll_movie_detail.addView(actressHolder.view)
        ll_movie_detail.addView(genreHolder.view)
        ll_movie_detail.addView(relativeMovieHolder.view)


    }

    private fun initData() {
        (intent.extras?.getSerializable(C.BundleKey.Key_1) as? Movie)?.let {
            movie = it
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        headHolder.release()
        sampleHolder.release()
        actressHolder.release()
        genreHolder.release()
        relativeMovieHolder.release()
        ImmersionBar.with(this).destroy()
    }

    override fun createPresenter() = MovieDetailPresenterImpl(intent?.getBooleanExtra(C.BundleKey.Key_2, false)
            ?: false)

    override val layoutId = R.layout.activity_movie_detail

    override var movie: Movie? = null


    override fun showLoading() {
        KLog.t(TAG).d("showLoading")
        sr_refresh?.let {
            if (!it.isRefreshing) {
                it.post {
                    it.setProgressViewOffset(false, 0, statusBarHeight)
                    it.isRefreshing = true
                }
            }
        } ?: super.showLoading()
    }

    override fun dismissLoading() {
        KLog.t(TAG).d("dismissLoading")
        sr_refresh?.let {
            it.post { it.isRefreshing = false }
        } ?: super.dismissLoading()
    }

    override fun <T> showContent(data: T?) {
        if (data is Movie && movie == null) {
            movie = data
            invalidateOptionsMenu()
        }

        if (data is MovieDetail) {
            //Slide Up Animation

            supportActionBar?.title = data.title
            //cover fixme
            iv_movie_cover.setOnClickListener { WatchLargeImageActivity.startShow(this, listOf(data.cover) + data.imageSamples.map { it.image }) }
            GlideApp.with(this)
                    .load(data.cover.toGlideNoHostUrl)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(DrawableImageViewTarget(iv_movie_cover))
            //animation
            ll_movie_detail.y = ll_movie_detail.y + 120
            ll_movie_detail.alpha = 0f
            ll_movie_detail.visibility = View.VISIBLE
            ll_movie_detail.animate().translationY(0f).alpha(1f).setDuration(500).start()

            headHolder.init(data.headers)
            sampleHolder.init(data.imageSamples)
            sampleHolder.cover = data.cover
            actressHolder.init(data.actress)
            genreHolder.init(data.genres)
            relativeMovieHolder.init(data.relatedMovies)


        }
    }

    /*===========================other===================================*/
    companion object {
        fun start(current: Context, movie: Movie, fromHistory: Boolean = false) {
            current.startActivity(Intent(current, MovieDetailActivity::class.java).apply {
                putExtra(C.BundleKey.Key_1, movie)
                putExtra(C.BundleKey.Key_2, fromHistory)
            })
        }

        fun start(current: Context, movieUrl: String) {
            current.startActivity(Intent(current, MovieDetailActivity::class.java).apply {
                putExtra(C.BundleKey.Key_1, movieUrl)
                putExtra(C.BundleKey.Key_2, false)
            })
        }

    }

}
