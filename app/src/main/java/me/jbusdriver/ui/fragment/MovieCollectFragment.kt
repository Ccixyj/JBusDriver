package me.jbusdriver.ui.fragment

import me.jbusdriver.mvp.MovieListContract
import me.jbusdriver.mvp.presenter.CollectMovieListPresenterImpl

/**
 * Created by Administrator on 2017/7/17 0017.
 */
class MovieCollectFragment : MovieListFragment() , MovieListContract.MovieListView {

    override fun createPresenter() =  CollectMovieListPresenterImpl()

    companion object {
        fun newInstance() = MovieCollectFragment()
    }
}