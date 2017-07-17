package me.jbusdriver.mvp.presenter

import me.jbusdriver.common.CollectManager
import me.jbusdriver.mvp.bean.Movie

class MovieCollectPresenterImpl : BaseAbsDataPresenter<Movie>() {

    override fun onFirstLoad() {
    }

    override fun getData() = CollectManager.movie_data

    override fun onStart(firstStart: Boolean) {
        super.onStart(firstStart)
        onRefresh()
    }
}