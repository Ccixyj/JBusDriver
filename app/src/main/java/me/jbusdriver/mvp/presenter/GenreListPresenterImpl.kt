package me.jbusdriver.mvp.presenter

import me.jbusdriver.base.mvp.presenter.BasePresenterImpl
import me.jbusdriver.mvp.GenreListContract

class GenreListPresenterImpl : BasePresenterImpl<GenreListContract.GenreListView>(), GenreListContract.GenreListPresenter {
    override fun onFirstLoad() {
        loadData4Page(1)//首次加载 可以从内存中读取
    }
    override fun loadData4Page(page: Int) {
        mView?.let {
            it.dismissLoading()
            it.resetList()
            it.showContents(it.data)
            it.loadMoreComplete()
            it.loadMoreEnd()
        }
    }

    override fun onLoadMore() = Unit
    override fun hasLoadNext() = false

    override fun onRefresh() {
        loadData4Page(1)
    }

    override fun lazyLoad() {
        onFirstLoad()
    }
}