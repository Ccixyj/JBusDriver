package me.jbusdriver.mvp.presenter

import me.jbusdriver.mvp.LinkListContract
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.ui.data.collect.ICollect

class MovieCollectPresenterImpl(collector: ICollect<Movie>) : BaseAbsCollectPresenter<LinkListContract.LinkListView, Movie>(collector), LinkListContract.LinkListPresenter {

    override fun loadAll(iaAll: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun jumpToPage(page: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun isPrevPageLoaded(currentPage: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lazyLoad() {
        onFirstLoad()
    }


}