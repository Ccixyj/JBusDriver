package me.jbusdriver.ui.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import com.afollestad.materialdialogs.MaterialDialog
import me.jbusdriver.common.KLog
import me.jbusdriver.common.toast
import me.jbusdriver.mvp.LinkListContract
import me.jbusdriver.mvp.presenter.MovieCollectPresenterImpl
import me.jbusdriver.ui.data.AppConfiguration
import me.jbusdriver.ui.data.collect.MovieCollector

/**
 * Created by Administrator on 2017/7/17 0017.
 */
class MovieCollectFragment : AbsMovieListFragment(), LinkListContract.LinkListView {

    override fun createPresenter() = MovieCollectPresenterImpl(MovieCollector)

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) = Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter.setOnItemChildLongClickListener { adapter, view, position ->
            KLog.d("setOnItemChildLongClickListener $position")
            this@MovieCollectFragment.adapter.getData().getOrNull(position)?.let {
                MaterialDialog.Builder(viewContext)
                        .title(it.title)
                        .items(listOf("取消收藏"))
                        .itemsCallback { _, _, _, text ->
                            if (MovieCollector.removeCollect(it)) {
                                viewContext.toast("取消收藏成功")
                                adapter.data.removeAt(position)
                                adapter.notifyItemRemoved(position)
                            } else {
                                viewContext.toast("已经取消了")
                            }
                        }
                        .show()
            }

            return@setOnItemChildLongClickListener true
        }

    }

    override val pageMode: Int = AppConfiguration.PageMode.Normal

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        MovieCollector.save()
    }

    companion object {
        fun newInstance() = MovieCollectFragment()
    }
}