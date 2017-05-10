package me.jbusdriver.ui.fragment

import android.os.Bundle
import android.view.MenuItem
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.common.C
import me.jbusdriver.mvp.MovieListContract
import me.jbusdriver.mvp.presenter.MovieListPresenterImpl
import me.jbusdriver.ui.data.DataSourceType


/**
 * Created by Administraor on 2017/4/9.
 */
class HomeMovieListFragment : MovieListFragment(), MovieListContract.MovieListView {
    override fun createPresenter() = MovieListPresenterImpl()
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the CENSORED/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        when (id) {
            R.id.action_show_all -> {
                item.isChecked = !item.isChecked
                if (item.isChecked) item.title = "已发布" else item.title = "全部电影"  /*false : 已发布的 ,true :全部*/
                mBasePresenter?.loadAll(item.isChecked)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /*================================================*/
    companion object {
        fun newInstance(type: DataSourceType) = HomeMovieListFragment().apply {
            arguments = Bundle().apply { putSerializable(C.BundleKey.Key_1, type) }
        }
    }

}