package me.jbusdriver.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.Toast
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.base.common.AppBaseActivity
import me.jbusdriver.base.common.C
import me.jbusdriver.db.bean.History
import me.jbusdriver.mvp.MovieParseContract
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.bean.des
import me.jbusdriver.mvp.presenter.MovieParsePresenterImpl
import me.jbusdriver.ui.fragment.LinkableListFragment
import me.jbusdriver.ui.fragment.LinkedMovieListFragment

class MovieListActivity : AppBaseActivity<MovieParseContract.MovieParsePresenter, MovieParseContract.MovieParseView>(), MovieParseContract.MovieParseView {


    override fun createPresenter() = MovieParsePresenterImpl()

    override val layoutId = R.layout.activity_moive_list

    private val linkData by lazy {
        intent.getSerializableExtra(C.BundleKey.Key_1) as? ILink ?: error("no link data")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolBar()
        supportFragmentManager.beginTransaction().replace(R.id.fl_container, LinkedMovieListFragment.newInstance(linkData).apply {
            arguments?.putAll(intent.extras)
        }).commitAllowingStateLoss()
    }

    private fun setToolBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
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

        /**
         * inline get() = when (this) {
        is Movie -> 1
        is ActressInfo -> 2
        is Header -> 3
        is Genre -> 4
        is SearchLink -> 5
        is PageLink -> 6
        else -> error(" $this has no matched class for des")
        }
        不需要加入历史记录
         */
        fun reloadFromHistory(context: Context, his: History) {
            context.startActivity(Intent(context, MovieListActivity::class.java).apply {
                putExtra(C.BundleKey.Key_1, his.getLinkItem())
                putExtra(C.BundleKey.Key_2, true)/*from History*/
                putExtra(LinkableListFragment.MENU_SHOW_ALL, his.isAll)
            })
        }
    }
}
