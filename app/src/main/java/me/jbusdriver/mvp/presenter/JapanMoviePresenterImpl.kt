package me.jbusdriver.mvp.presenter

import me.jbusdriver.mvp.JapanMovieContract
import me.jbusdriver.mvp.JapanMovieContract.JapanMovieView

class JapanMoviePresenterImpl : AbstractRefreshLoadMorePresenterImpl<JapanMovieView>(), JapanMovieContract.JapanMoviePresenter {


    
    override fun loadData4Page(page: Int) {
       
    }

}