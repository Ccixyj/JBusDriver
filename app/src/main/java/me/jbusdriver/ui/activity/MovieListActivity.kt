package me.jbusdriver.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.Toast
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.common.AppBaseActivity
import me.jbusdriver.common.C
import me.jbusdriver.mvp.MovieParseContract
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.bean.des
import me.jbusdriver.mvp.presenter.MovieParsePresenterImpl
import me.jbusdriver.ui.fragment.LinkedMovieListFragment

class MovieListActivity : AppBaseActivity<MovieParseContract.MovieParsePresenter, MovieParseContract.MovieParseView>(), MovieParseContract.MovieParseView {


    override fun createPresenter() = MovieParsePresenterImpl()

    override val layoutId = R.layout.activity_moive_list

    private val linkData by lazy { intent.getSerializableExtra(C.BundleKey.Key_1) as? ILink ?: error("no link data") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolBar()
        supportFragmentManager.beginTransaction().replace(R.id.fl_container, LinkedMovieListFragment.newInstance(linkData, true))
                .commitAllowingStateLoss()
    }

    private fun setToolBar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = linkData.des
    }

    override fun <T> showContent(data: T?) {

    }

    companion object {
        fun start(context: Context, it: ILink) {
            if (it.link.isNotBlank()) {
                context.startActivity(Intent(context, MovieListActivity::class.java).apply {
                    putExtra(C.BundleKey.Key_1, it)
                })
            } else {
                Toast.makeText(context, "没有可用的跳转链接", Toast.LENGTH_LONG).show()
            }

        }
    }
}
