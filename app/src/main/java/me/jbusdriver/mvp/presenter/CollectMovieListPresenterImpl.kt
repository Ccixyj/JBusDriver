package me.jbusdriver.mvp.presenter

import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import me.jbusdriver.common.CollectManager
import me.jbusdriver.common.KLog
import me.jbusdriver.common.SchedulersCompat
import me.jbusdriver.common.SimpleSubscriber
import me.jbusdriver.mvp.MovieListContract
import me.jbusdriver.mvp.bean.PageInfo
import me.jbusdriver.mvp.bean.hasNext

/**
 * Created by Administrator on 2017/5/10 0010.
 */


class CollectMovieListPresenterImpl : BasePresenterImpl<MovieListContract.MovieListView>(), MovieListContract.MovieListPresenter {

    private val PageSize = 5
    private var pageInfo = PageInfo()
    private val data by lazy { CollectManager.movie_data.toMutableList() }
    private val pageNum by lazy { (data.size / PageSize) + 1 }

    override fun loadAll(iaAll: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onFirstLoad() {
        super.onFirstLoad()
        loadData4Page(1)
    }

    override fun loadData4Page(page: Int) {
        val next = if (page < pageNum) page + 1 else pageNum
        pageInfo = pageInfo.copy(page, next)
        Flowable.just(pageInfo).map {
            KLog.d("request page : $it")
            val start = (pageInfo.activePage - 1) * PageSize
            val nextSize = start + PageSize
            val end = if (nextSize <= data.size) nextSize else data.size
            data.subList(start, end)
        }.compose(SchedulersCompat.io())
                .subscribeWith(DefaultSubscriber(page))
                .addTo(rxManager)

    }

    override fun onLoadMore() {
        if (hasLoadNext()) loadData4Page(pageInfo.nextPage)
        else mView?.loadMoreEnd()
    }

    override fun hasLoadNext() = pageInfo.hasNext

    override fun onRefresh() {
        data.clear()
        data.addAll(CollectManager.movie_data)
        loadData4Page(1)
    }

     open inner class DefaultSubscriber(val pageIndex: Int) : SimpleSubscriber<List<Any>>() {

        override fun onStart() {
            AndroidSchedulers.mainThread().scheduleDirect {
                (pageIndex == 1).let {
                    if (it) mView?.enableLoadMore(false) else mView?.enableRefresh(false)
                }
                if (pageIndex == 1) mView?.showLoading()
            }
            super.onStart()
        }

        override fun onComplete() {
            super.onComplete()
            if (!hasLoadNext()) {
                mView?.loadMoreEnd() //判断是否加载完毕
            }
            mView?.dismissLoading()
            (pageIndex == 1).let {
                if (it) mView?.enableLoadMore(true) else mView?.enableRefresh(true)
            }
            if (pageIndex != pageInfo.activePage) {
                KLog.w("page $pageIndex is mess : $pageInfo")
                pageInfo = pageInfo.copy(pageIndex)
            }

        }

        override fun onError(e: Throwable) {
            super.onError(e)
            mView?.dismissLoading()
            mView?.loadMoreFail()
            mView?.showError(e)
            //page 重置成前一页
            (pageIndex == 1).let {
                if (it) mView?.enableLoadMore(true) else mView?.enableRefresh(true)
            }
            pageInfo = PageInfo(pageIndex - 1, pageIndex)
        }

        override fun onNext(t: List<Any>) {
            super.onNext(t)
            if (pageIndex == 1) {
                mView?.resetList()
            }
            mView?.showContents(t)
            if (pageIndex > 1) mView?.loadMoreComplete()
        }
    }

}