package me.jbusdriver.mvp.presenter

import android.text.TextUtils
import io.reactivex.Flowable
import io.reactivex.functions.Function
import io.reactivex.rxkotlin.addTo
import me.jbusdriver.base.*
import me.jbusdriver.base.mvp.BaseView
import me.jbusdriver.base.mvp.model.BaseModel
import me.jbusdriver.base.mvp.presenter.BasePresenter
import me.jbusdriver.mvp.bean.PageInfo
import me.jbusdriver.mvp.bean.ResultPageBean
import me.jbusdriver.mvp.bean.hasNext
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
        when {
            hasLoadNext() -> loadData4Page(pageInfo.nextPage)
            pageInfo.nextPage >= Math.max(pageInfo.activePage, pageInfo.referPages.lastOrNull()
                    ?: 1) -> {
                lastPage = pageInfo.activePage
                mView?.loadMoreEnd()
            }
            else -> Unit
        }
    }

    override fun hasLoadNext(): Boolean = pageInfo.hasNext

    override fun onRefresh() {
        rxManager.clear()
        loadData4Page(1)
    }

    override fun loadData4Page(page: Int) {
        var curPage = pageInfo.copy(activePage = page, nextPage = page) //设置相等,防止 onload more
        val request = (if (page == 1) model.requestFromCache(page)
        else model.requestFor(page))
        request.map { doc ->
            parsePage(doc)?.let {
                curPage = it
            }
            ResultPageBean(curPage, stringMap(curPage, doc))

        }.onErrorResumeNext(Function {
            return@Function when {
                (it is HttpException && it.code() == 404) -> {
                    //404 视为没有数据
                    mView?.viewContext?.toast("第${page}页没有数据")
                    if (curPage.nextPage > 1 && curPage.activePage > 0) pageInfo = pageInfo.copy(activePage = pageInfo.nextPage - 1)//重置前一页pageInfo
                    Flowable.just(ResultPageBean.emptyPage(curPage))
                }
                it is TimeoutException -> {
                    mView?.viewContext?.toast("第${page}页请求超时,请过会再次尝试")
                    Flowable.just(ResultPageBean.emptyPage(curPage))
                }
                else -> throw  it
            }
        }).compose(SchedulersCompat.io())
                .subscribeWith(ListDefaultSubscriber(curPage))
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
                    , pages).apply {
                if (pages.isNotEmpty()) {
                    lastPage = pages.last()

                }
            }
        }
    }

    abstract fun stringMap(pageInfo: PageInfo, str: Document): List<T>

    /**
     * 加载列表默认实现的订阅者.
     * 实现@AbstractRefreshLoadMorePresenterImpl 可直接使用该类.
     */

    protected open fun doAddData(t: ResultPageBean<T>) {
//        if (t.isNotEmpty()) {
        mView?.showContents(t.data)
//        }
        if (pageInfo.activePage > 1) mView?.loadMoreComplete()
    }

    open inner class ListDefaultSubscriber(private val currentPage: PageInfo) : SimpleSubscriber<ResultPageBean<T>>() {

        private val old = pageInfo //保存旧值

        override fun onStart() {
            pageInfo = currentPage
            postMain {
                (currentPage.activePage == 1).let {
                    if (it) mView?.enableLoadMore(false) else mView?.enableRefresh(false)
                }
                if (currentPage.activePage == 1) mView?.showLoading()
            }
            super.onStart()
        }

        override fun onComplete() {
            super.onComplete()
            if (!hasLoadNext()) {
                if (currentPage.activePage == lastPage) {
                    mView?.loadMoreEnd(false) //判断是否加载完毕
                } else {
                    mView?.loadMoreEnd(true)
                }
            } else {
                if (currentPage.activePage == lastPage) mView?.loadMoreEnd()
            }

            mView?.dismissLoading()
            (currentPage.activePage == 1).let {
                if (it) mView?.enableLoadMore(true) else mView?.enableRefresh(true)
            }
            if (currentPage.activePage != pageInfo.activePage) {
                KLog.w("page ${currentPage.activePage} is mess : $pageInfo")
//                pageInfo = pageInfo.copy(activePage = pageIndex)
            }

        }

        override fun onError(e: Throwable) {
            super.onError(e)
            mView?.dismissLoading()
            mView?.loadMoreFail()
            mView?.showError(e)
            //page 重置成前一页
            (currentPage.activePage == 1).let {
                if (it) mView?.enableLoadMore(true) else mView?.enableRefresh(true)
                mView?.resetList()
            }
            pageInfo = old
        }

        override fun onNext(t: ResultPageBean<T>) {
            super.onNext(t)
            if (currentPage.activePage == 1) {
                mView?.resetList()
            }
            pageInfo = t.pageInfo //考虑cancel的情况,所以在onnext赋值,
            doAddData(t)
            mView?.dismissLoading()
        }
    }


}
