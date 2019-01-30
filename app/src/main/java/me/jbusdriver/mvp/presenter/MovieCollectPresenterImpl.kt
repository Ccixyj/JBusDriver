package me.jbusdriver.mvp.presenter

import me.jbusdriver.mvp.MovieCollectContract
import me.jbusdriver.mvp.bean.Movie

class MovieCollectPresenterImpl : BaseAbsCollectPresenter<MovieCollectContract.MovieCollectView, Movie>(),
    MovieCollectContract.MovieCollectPresenter {


    override fun lazyLoad() {
        onFirstLoad()
    }


}