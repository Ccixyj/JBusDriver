package me.jbusdriver.mvp.presenter

import me.jbusdriver.ui.data.CollectManager
import me.jbusdriver.mvp.MovieListContract
import me.jbusdriver.mvp.bean.Movie

class MovieCollectPresenterImpl : BaseAbsDataPresenter<MovieListContract.MovieListView,Movie>() ,MovieListContract.MovieListPresenter{

    override fun loadAll(iaAll: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onFirstLoad() {
    }

    override fun getData() = CollectManager.movie_data

    override fun onStart(firstStart: Boolean) {
        super.onStart(firstStart)
        onRefresh()
    }
}