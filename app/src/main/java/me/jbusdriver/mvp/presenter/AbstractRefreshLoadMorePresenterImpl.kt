package me.jbusdriver.mvp.presenter

import android.text.TextUtils
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.rxkotlin.addTo
import me.jbusdriver.common.KLog
import me.jbusdriver.common.SchedulersCompat
import me.jbusdriver.common.SimpleSubscriber
import me.jbusdriver.common.toast
import me.jbusdriver.mvp.BaseView
import me.jbusdriver.mvp.bean.PageInfo
import me.jbusdriver.mvp.bean.hasNext
import me.jbusdriver.mvp.model.BaseModel
import org.jsoup.nodes.Document
import retrofit2.HttpException
import java.util.concurrent.TimeoutException

/**
 * Created by Administrator on 2016/9/6 0006.
 * 通用下拉加在更多 , 上拉刷新处理 处理,可单独使用其中一部分
 */
abstract class AbstractRefreshLoadMorePresenterImpl<V : BaseView.BaseListWithRefreshView, T> : BasePresenterImpl<V>(), BasePresenter.BaseRefreshLoadMorePresenter<V> {

    protected var pageInfo = PageInfo()
    protected var lastPage: Int = Int.MAX_VALUE

    abstract val model: BaseModel<Int, Document>

    override fun onFirstLoad() {
        loadData4Page(1)//首次加载 可以从内存中读取
    }


    override fun onLoadMore() {
        KLog.i("onLoadMore :${hasLoadNext()} ; page :$pageInfo")
        if (hasLoadNext()) loadData4Page(pageInfo.nextPage)
        else if (pageInfo.nextPage == pageInfo.activePage) {
            if (pageInfo.pages.isEmpty() || (pageInfo.pages.isNotEmpty() && pageInfo.activePage <= pageInfo.pages.last())) {
                lastPage = pageInfo.activePage
                mView?.loadMoreEnd()
            }
        } else {

        }
    }

    override fun hasLoadNext(): Boolean = pageInfo.hasNext

    override fun onRefresh() {
        rxManager.clear()
        pageInfo = PageInfo(1, 0)
        loadData4Page(1)
    }

    override fun loadData4Page(page: Int) {
        val request = (if (page == 1) model.requestFromCache(page)
        else model.requestFor(page))
        request.map { doc ->
            parsePage(doc)?.let {
                KLog.i("parse page $it")
                pageInfo = it
            }
            stringMap(doc)
        }.onErrorResumeNext(Function {
            return@Function when {
                (it is HttpException && it.code() == 404) -> {
                    //404 视为没有数据
                    AndroidSchedulers.mainThread().scheduleDirect {
                        mView?.viewContext?.toast("第${page}页没有数据")
                    }
                    if (pageInfo.nextPage > 1 && pageInfo.activePage > 0) pageInfo = pageInfo.copy(activePage = pageInfo.nextPage - 1)//重置前一页pageInfo
                    Flowable.just(mutableListOf())
                }
                it is TimeoutException -> {
                    AndroidSchedulers.mainThread().scheduleDirect {
                        mView?.viewContext?.toast("第${page}页请求超时,请过会再次尝试")
                    }
                    Flowable.just(mutableListOf())
                }
                else -> throw  it
            }
        }).compose(SchedulersCompat.io())
                .subscribeWith(ListDefaultSubscriber(page))
                .addTo(rxManager)

    }

    private fun parsePage(pageDoc: Document): PageInfo? {
        with(pageDoc) {
            val current = select(".pagination .active > a").attr("href")
            if (TextUtils.isEmpty(current)) {
                lastPage = pageInfo.activePage
                return null
            }

            val next = select(".pagination .active ~ li >a").let {
                if (it.isEmpty()) current
                else it.attr("href")
            }
            val pages = select(".pagination a:not([id])").mapNotNull { it.attr("href").split("/").lastOrNull()?.toIntOrNull() }


            return PageInfo(current.split("/").lastOrNull()?.toIntOrNull() ?: 0,
                    next.split("/").lastOrNull()?.toIntOrNull() ?: 0
                    , current, next, pages).apply {
                if (pages.isNotEmpty()) {
                    if (activePage == nextPage && activePage == pages.last()) {
                        lastPage = pages.last()
                    }
                    //pages小于10页可以认为最大页就是pages的最后一个
                    pages.last().let {
                        if (it < 10) lastPage = it
                    }
                }
            }
        }
    }

    abstract fun stringMap(str: Document): List<T>

    /**
     * 加载列表默认实现的订阅者.
     * 实现@AbstractRefreshLoadMorePresenterImpl 可直接使用该类.
     */

    protected open fun doAddData(t: List<T>) {
//        if (t.isNotEmpty()) {
            mView?.showContents(t)
//        }
        if (pageInfo.activePage > 1) mView?.loadMoreComplete()
    }

    open inner class ListDefaultSubscriber(open val pageIndex: Int) : SimpleSubscriber<List<T>>() {

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
                if (pageIndex == lastPage) {
                    mView?.loadMoreEnd(false) //判断是否加载完毕
                } else {
                    mView?.loadMoreEnd(true)
                }
            } else {
                if (pageIndex == lastPage) mView?.loadMoreEnd()
            }

            mView?.dismissLoading()
            (pageIndex == 1).let {
                if (it) mView?.enableLoadMore(true) else mView?.enableRefresh(true)
            }
            if (pageIndex != pageInfo.activePage) {
                KLog.w("page $pageIndex is mess : $pageInfo")
//                pageInfo = pageInfo.copy(activePage = pageIndex)
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
