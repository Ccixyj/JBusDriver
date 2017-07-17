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
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.mvp.presenter.MovieParsePresenterImpl
import me.jbusdriver.ui.data.DataSourceType
import me.jbusdriver.ui.fragment.SimpleMovieListFragment

class MovieListActivity : AppBaseActivity<MovieParseContract.MovieParsePresenter, MovieParseContract.MovieParseView>(), MovieParseContract.MovieParseView {


    override fun createPresenter() = MovieParsePresenterImpl(linkData)

    override val layoutId = R.layout.activity_moive_list

    val type by lazy { intent.getSerializableExtra(C.BundleKey.Key_1) as? DataSourceType ?: DataSourceType.CENSORED }
    val linkData by lazy { intent.getSerializableExtra(C.BundleKey.Key_2) as? ILink ?: error("no link data") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolBar()
        supportFragmentManager.beginTransaction().replace(R.id.fl_container, SimpleMovieListFragment.newInstance(type, linkData))
                .commitAllowingStateLoss()
    }

    private fun setToolBar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        when (linkData) {
            is Header -> {
                (linkData as Header).let {
                    supportActionBar?.title = "${it.name}${it.value}"
                }
            }
            is Genre -> {
                (linkData as Genre).let {
                    supportActionBar?.title = "类别 ${it.name}"
                }
            }
            is ActressInfo -> {
                (linkData as ActressInfo).let {
                    supportActionBar?.title = "演员 ${it.name}"
                }
            }
            else -> {
                //no op
            }
        }
    }

    override fun <T> showContent(data: T?) {

    }

    companion object {
        fun start(context: Context, type: DataSourceType, it: ILink) {
            if (it.link.isNotBlank()) {
                context.startActivity(Intent(context, MovieListActivity::class.java).apply {
                    putExtra(C.BundleKey.Key_1, type)
                    putExtra(C.BundleKey.Key_2, it)
                })
            } else {
                Toast.makeText(context, "没有可用的跳转链接", Toast.LENGTH_LONG).show()
            }

        }
    }
}
