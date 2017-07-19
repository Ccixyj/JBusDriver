package me.jbusdriver.ui.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
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




    /*================================================*/
    companion object {
        fun newInstance(type: DataSourceType) = HomeMovieListFragment().apply {
            arguments = Bundle().apply { putSerializable(C.BundleKey.Key_1, type) }
        }
    }

}