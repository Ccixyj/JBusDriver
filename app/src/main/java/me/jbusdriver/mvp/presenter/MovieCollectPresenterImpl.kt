package me.jbusdriver.mvp.presenter

import me.jbusdriver.common.CollectManager
import me.jbusdriver.mvp.bean.Movie

class MovieCollectPresenterImpl : BaseAbsDataPresenter<Movie>() {

    override fun getData() = CollectManager.movie_data
}