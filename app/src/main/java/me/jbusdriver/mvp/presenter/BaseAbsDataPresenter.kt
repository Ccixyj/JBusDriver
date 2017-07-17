package me.jbusdriver.mvp.presenter

import com.cfzx.mvp.view.BaseView
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import me.jbusdriver.common.KLog
import me.jbusdriver.common.SchedulersCompat
import me.jbusdriver.common.SimpleSubscriber
import me.jbusdriver.mvp.bean.PageInfo
import me.jbusdriver.mvp.model.BaseModel
import org.jsoup.nodes.Document

/**
 * Created by Administrator on 2017/5/10 0010.
 */


abstract class BaseAbsDataPresenter<V:BaseView.BaseListWithRefreshView,T> : AbstractRefreshLoadMorePresenterImpl<V>() {

    private val PageSize = 20
    private val listData by lazy { getData().toMutableList() }
    private val pageNum
        get() = (listData.size / PageSize) + 1


    abstract fun getData():List<T>


    override fun loadData4Page(page: Int) {
        val next = if (page < pageNum) page + 1 else pageNum
        pageInfo = pageInfo.copy(page, next)
        Flowable.just(pageInfo).map {
            KLog.d("request page : $it")
            val start = (pageInfo.activePage - 1) * PageSize
            val nextSize = start + PageSize
            val end = if (nextSize <= listData.size) nextSize else listData.size
            listData.subList(start, end)
        }.compose(SchedulersCompat.io())
                .subscribeWith(DefaultSubscriber(page))
                .addTo(rxManager)

    }

    override fun onRefresh() {
        pageInfo = PageInfo()
        listData.clear()
        listData.addAll(getData())
        loadData4Page(1)
    }
    override val model: BaseModel<Int, Document>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun stringMap(str: Document): List<Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
            }
            pageInfo = PageInfo(pageIndex - 1, pageIndex)
        }

        override fun onNext(t: List<T>) {
            super.onNext(t)
            if (pageIndex == 1) {
                mView?.resetList()
            }
            mView?.showContents(t)
            if (pageIndex > 1) mView?.loadMoreComplete()
        }
    }

}