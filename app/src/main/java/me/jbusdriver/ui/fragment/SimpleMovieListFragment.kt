package me.jbusdriver.ui.fragment

import android.os.Bundle
import me.jbusdriver.common.C
import me.jbusdriver.mvp.MovieListContract
import me.jbusdriver.mvp.presenter.SimpleMovieListPresenter
import me.jbusdriver.ui.data.DataSourceType


/**
 * Created by Administraor on 2017/4/9.
 */
class SimpleMovieListFragment : MovieListFragment(), MovieListContract.MovieListView {


    val url by lazy { arguments.getString(C.BundleKey.Key_2) }


    override fun createPresenter() = SimpleMovieListPresenter(url)
    /*================================================*/

    companion object {
        fun newInstance(type: DataSourceType, url: String) = SimpleMovieListFragment().apply {
            arguments = Bundle().apply {
                putSerializable(C.BundleKey.Key_1, type)
                putString(C.BundleKey.Key_2, url)
            }
        }
    }

}