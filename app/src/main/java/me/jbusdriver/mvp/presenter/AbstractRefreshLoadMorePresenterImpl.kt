package me.jbusdriver.mvp.presenter

import com.cfzx.mvp.view.BaseView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import me.jbusdriver.common.KLog
import me.jbusdriver.common.SchedulersCompat
import me.jbusdriver.common.SimpleSubscriber
import me.jbusdriver.mvp.bean.PageInfo
import me.jbusdriver.mvp.bean.hasNext
import me.jbusdriver.mvp.model.BaseModel
import org.jsoup.nodes.Document

/**
 * Created by Administrator on 2016/9/6 0006.
 * 通用下拉加在更多 , 上拉刷新处理 处理,可单独使用其中一部分
 */
abstract class AbstractRefreshLoadMorePresenterImpl<V : BaseView.BaseListWithRefreshView, T> : BasePresenterImpl<V>(), BasePresenter.BaseRefreshLoadMorePresenter<V> {

    protected var pageInfo = PageInfo()

    abstract val model: BaseModel<Int, Document>

    override fun onFirstLoad() {
        loadData4Page(1)//首次加载 可以从内存中读取
    }


    override fun onLoadMore() {
        KLog.d("onLoadMore :${hasLoadNext()} ; page :$pageInfo")
        if (hasLoadNext()) loadData4Page(pageInfo.nextPage)
        else if (pageInfo.nextPage == pageInfo.activePage && pageInfo.activePage > 0) mView?.loadMoreEnd()
        else {
            //
        }
    }

    override fun hasLoadNext(): Boolean = pageInfo.hasNext

    override fun onRefresh() {
        rxManager.clear()
        loadData4Page(1)
    }

    override fun loadData4Page(page: Int) {
        val request = (if (page == 1) model.requestFromCache(page)
        else model.requestFor(page))
        request.map {
            pageInfo = parsePage(it)
            KLog.d("parse page :$pageInfo")
            stringMap(it)
        }.compose(SchedulersCompat.io())
                .subscribeWith(DefaultSubscriber(page))
                .addTo(rxManager)

    }

    fun parsePage(pageDoc: Document): PageInfo {
        with(pageDoc) {
            val current = select(".pagination .active > a").attr("href")
            val next = select(".pagination .active ~ li >a").let {
                if (it.isEmpty()) current
                else it.attr("href")
            }

            val pages = select(".pagination a:not([id])").mapNotNull { it.attr("href").split("/").lastOrNull()?.toIntOrNull() }
            return PageInfo(current.split("/").lastOrNull()?.toIntOrNull() ?: 0,
                    next.split("/").lastOrNull()?.toIntOrNull() ?: 0
                    , current, next, pages)
        }
    }

    abstract fun stringMap(str: Document): List<T>

    /**
     * 加载列表默认实现的订阅者.
     * 实现@AbstractRefreshLoadMorePresenterImpl 可直接使用该类.
     */

    protected open fun doAddData(t: List<T>) {
        mView?.showContents(t)
        if (pageInfo.activePage > 1) mView?.loadMoreComplete()
    }

    open inner class DefaultSubscriber(val pageIndex: Int) : SimpleSubscriber<List<T>>() {

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
                mView?.resetList()
            }
            pageInfo = PageInfo(pageIndex - 1, pageIndex)
        }

        override fun onNext(t: List<T>) {
            super.onNext(t)
            if (pageIndex == 1) {
                mView?.resetList()
            }
            doAddData(t)
        }
    }
}
