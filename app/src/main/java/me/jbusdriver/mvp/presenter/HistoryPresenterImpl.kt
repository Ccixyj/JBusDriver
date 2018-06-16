package me.jbusdriver.mvp.presenter

import io.reactivex.BackpressureStrategy
import io.reactivex.rxkotlin.addTo
import me.jbusdriver.base.SchedulersCompat
import me.jbusdriver.db.bean.History
import me.jbusdriver.db.service.HistoryService
import me.jbusdriver.mvp.HistoryContract
import me.jbusdriver.mvp.bean.PageInfo
import me.jbusdriver.mvp.bean.ResultPageBean
import me.jbusdriver.base.mvp.model.BaseModel
import org.jsoup.nodes.Document

class HistoryPresenterImpl : AbstractRefreshLoadMorePresenterImpl<HistoryContract.HistoryView, History>(), HistoryContract.HistoryPresenter {

    private val dbPage by lazy { HistoryService.page() }

    override fun loadData4Page(page: Int) {
        val dbPage = dbPage.copy(currentPage = page).apply {
            lastPage = totalPage
        }
        HistoryService.queryPage(dbPage).toFlowable(BackpressureStrategy.DROP)
                .map {
                    ResultPageBean(pageInfo.copy(activePage = dbPage.currentPage, nextPage = dbPage.currentPage + 1), it)
                }
                .compose(SchedulersCompat.io())
                .subscribeWith(object : ListDefaultSubscriber(PageInfo(page)) {

                    override fun onNext(t: ResultPageBean<History>) {
                        super.onNext(t)
                        if (page >= dbPage.totalPage) mView?.loadMoreEnd()
                        mView?.dismissLoading()
                        (page == 1).let {
                            if (it) mView?.enableLoadMore(true) else mView?.enableRefresh(true)
                        }
                    }
                })
                .addTo(rxManager)
    }

    override fun clearHistory() {
        HistoryService.clearAll()
    }

    override fun onRefresh() {
        loadData4Page(1)
    }

    override fun lazyLoad() {
        onFirstLoad()
    }

    override val model: BaseModel<Int, Document>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun stringMap(page: PageInfo, str: Document): List<History> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}