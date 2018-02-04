package me.jbusdriver.mvp.presenter

import me.jbusdriver.mvp.MovieCollectContract
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.ui.data.collect.ICollect

class MovieCollectPresenterImpl(collector: ICollect<Movie>) : BaseAbsCollectPresenter<MovieCollectContract.MovieCollectView, Movie>(collector), MovieCollectContract.MovieCollectPresenter {


    override fun lazyLoad() {
        onFirstLoad()
    }


}