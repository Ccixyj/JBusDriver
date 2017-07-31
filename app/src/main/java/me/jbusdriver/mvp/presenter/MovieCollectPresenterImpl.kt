package me.jbusdriver.mvp.presenter

import me.jbusdriver.mvp.LinkListContract
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.ui.data.CollectManager

class MovieCollectPresenterImpl : BaseAbsCollectPresenter<LinkListContract.LinkListView,Movie>() , LinkListContract.LinkListPresenter {

    override fun loadAll(iaAll: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun getData() = CollectManager.movie_data

    override fun lazyLoad() {
        onRefresh()
    }
}