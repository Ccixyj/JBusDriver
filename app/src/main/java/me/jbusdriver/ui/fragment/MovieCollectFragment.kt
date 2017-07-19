package me.jbusdriver.ui.fragment

import android.view.Menu
import android.view.MenuInflater
import me.jbusdriver.mvp.MovieListContract
import me.jbusdriver.mvp.presenter.MovieCollectPresenterImpl

/**
 * Created by Administrator on 2017/7/17 0017.
 */
class MovieCollectFragment : MovieListFragment() , MovieListContract.MovieListView {

    override fun createPresenter() =  MovieCollectPresenterImpl()

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) = Unit

    companion object {
        fun newInstance() = MovieCollectFragment()
    }
}