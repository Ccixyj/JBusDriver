package me.jbusdriver.mvp.presenter

import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import me.jbusdriver.common.KLog
import me.jbusdriver.common.SchedulersCompat
import me.jbusdriver.mvp.HistoryContract
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.bean.PageInfo
import me.jbusdriver.mvp.model.BaseModel
import me.jbusdriver.ui.data.collect.LinkCollector
import org.jsoup.nodes.Document

class HistoryPresenterImpl :AbstractRefreshLoadMorePresenterImpl<HistoryContract.HistoryView,ILink>(), HistoryContract.HistoryPresenter{

    private val PageSize = 20
    private val listData by lazy { LinkCollector.dataList.toMutableList() }
    private val pageNum
        get() = (listData.size / PageSize) + 1

    override fun loadData4Page(page: Int) {
        val next = if (page < pageNum) page + 1 else pageNum
        pageInfo = pageInfo.copy(activePage = page, nextPage = next)
        Flowable.just(pageInfo).map {
            KLog.d("request page : $it")
            val start = (pageInfo.activePage - 1) * PageSize
            val nextSize = start + PageSize
            val end = if (nextSize <= listData.size) nextSize else listData.size
            listData.subList(start, end)
        }.compose(SchedulersCompat.io())
                .subscribeWith(ListDefaultSubscriber(page))
                .addTo(rxManager)

    }

    override fun onRefresh() {
        pageInfo = PageInfo()
        listData.clear()
        listData.addAll(LinkCollector.dataList)
        loadData4Page(1)
    }

    override val model: BaseModel<Int, Document>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun stringMap(str: Document): List<ILink> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}