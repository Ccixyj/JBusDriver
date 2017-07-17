package me.jbusdriver.ui.fragment

import me.jbusdriver.mvp.MovieListContract
import me.jbusdriver.mvp.presenter.MovieCollectPresenterImpl

/**
 * Created by Administrator on 2017/7/17 0017.
 */
class MovieCollectFragment : MovieListFragment() , MovieListContract.MovieListView {

    override fun createPresenter() =  MovieCollectPresenterImpl()

    companion object {
        fun newInstance() = MovieCollectFragment()
    }
}