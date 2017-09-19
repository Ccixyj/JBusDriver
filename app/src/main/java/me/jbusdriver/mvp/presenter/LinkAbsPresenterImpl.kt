package me.jbusdriver.mvp.presenter

import com.cfzx.utils.CacheLoader
import io.reactivex.Flowable
import io.reactivex.rxkotlin.subscribeBy
import me.jbusdriver.common.*
import me.jbusdriver.db.bean.History
import me.jbusdriver.db.service.HistoryService
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.LinkListContract
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.mvp.model.BaseModel
import me.jbusdriver.ui.data.AppConfiguration
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap

/**
 * Created by Administrator on 2017/5/10 0010.
 */
abstract class LinkAbsPresenterImpl<T>(val linkData: ILink, val isHistory: Boolean = false) : AbstractRefreshLoadMorePresenterImpl<LinkListContract.LinkListView, T>(), LinkListContract.LinkListPresenter {

    protected open var IsAll = false
    private val urlPath by lazy { if (linkData is PageLink) linkData.link.urlPath.replace("/${linkData.page}", "") else linkData.link.urlPath }
    private val dataPageCache by lazy { ConcurrentSkipListMap<Int, Int>() }
    private val pageModeDisposable = RxBus.toFlowable(PageChangeEvent::class.java)
            .subscribeBy(onNext = {
                KLog.d("PageChangeEvent $it")

                onRefresh()
                //清空dataPageCache
                dataPageCache.clear()
            })
    private val historyService by lazy { HistoryService() }

    override fun onFirstLoad() {
        val link = when (linkData) {
            is PageLink -> {
                pageInfo = PageInfo(linkData.page, linkData.page + 1)
                loadData4Page(linkData.page)//首次加载 可以从内存中读取
                linkData.copy(linkData.page, mView?.type?.key ?: "")
            }
            else -> {
                loadData4Page(1)
                linkData
            }
        }
        if (!isHistory) addHistory(link)
    }

    override fun loadAll(iaAll: Boolean) {
        IsAll = iaAll
        dataPageCache.clear()
        loadData4Page(1)
        val link = when (linkData) {
            is PageLink -> linkData.copy(1, mView?.type?.key ?: "")
            else -> linkData
        }
        if (!isHistory) addHistory(link)
    }

    /*不需要*/
    override val model: BaseModel<Int, Document> = object : BaseModel<Int, Document> {
        override fun requestFor(t: Int) =
                (if (t == 1) linkData.link else "${linkData.link.urlHost}$urlPath/$t").let {
                    KLog.i("fromCallable page $pageInfo requestFor : $it")
                    JAVBusService.INSTANCE.get(it, if (IsAll) "all" else null).map { Jsoup.parse(it) }
                }.doOnNext {
                    if (t == 1) CacheLoader.lru.put("${linkData.link}$IsAll", it.toString())
                }

        override fun requestFromCache(t: Int) = Flowable.concat(CacheLoader.justLru(linkData.link).map { Jsoup.parse(it) }, requestFor(t))
                .firstOrError().toFlowable()
    }

    protected open fun addHistory(link: ILink) {
        historyService.insert(History(link.DBtype, Date(), link.toJsonString(), IsAll))
    }

    override fun onRefresh() {
        dataPageCache.clear()
        CacheLoader.lru.remove(linkData.link)
        super.onRefresh()
    }

    override fun lazyLoad() {
        onFirstLoad()
    }

    override fun jumpToPage(page: Int) {
        KLog.i("jumpToPage $page in $dataPageCache")
        if (page >= 1) {

            if (page > lastPage) {
                mView?.viewContext?.toast("当前最多${lastPage}页")
                return
            }

            if (dataPageCache.containsKey(page)) {
                //已经加载直接跳页
                mView?.moveTo(getJumpIndex(page))
            } else {
                mView?.moveTo(getJumpIndex(page))
                pageInfo = pageInfo.copy(page)
                loadData4Page(page)
            }
        } else {
            KLog.w("can't jump to page $page")
        }
    }


    override fun isPrevPageLoaded(currentPage: Int): Boolean {
        return AppConfiguration.pageMode == AppConfiguration.PageMode.Page && currentPage > 2 &&
                !dataPageCache.containsKey(currentPage - 1)
    }

    override fun onLoadMore() {
        //跳转后pageinfo可能不是线性的了,所以需要根据dataPageCache来判断
        if (dataPageCache.isNotEmpty() && dataPageCache.lastKey() >= pageInfo.nextPage) {
            //加载过比他大的页面
            //直接完成
            pageInfo = PageInfo(dataPageCache.lastKey(), dataPageCache.lastKey() + 1)
            mView?.loadMoreComplete()
            if (pageInfo.nextPage >= lastPage) {
                //加载完毕
                mView?.loadMoreEnd()
            }
            return
        }

        super.onLoadMore()
    }


    override fun doAddData(t: List<T>) {

        synchronized(pageInfo) {
            when (mView?.pageMode) {
                AppConfiguration.PageMode.Page -> {
                    //需要判断数据
                    if (dataPageCache.keys.contains(pageInfo.activePage)) {
                        mView?.loadMoreComplete() //直接加载完成
                        if (pageInfo.activePage >= lastPage) mView?.loadMoreEnd()
                        return
                    }
                    if (dataPageCache.size > 0 && pageInfo.activePage < dataPageCache.lastKey()) {
                        //当前页属于插入页面
                        mView?.insertDatas(getJumpIndex(pageInfo.activePage), t)
                    } else {
                        super.doAddData(t)
                    }

                    if (t.isNotEmpty()) {
                        dataPageCache.put(pageInfo.activePage, t.size - 1)//page item In list
                    } else {
                        //超过最大页数时 ,可以点击加载原本的下一页 ; 或者请求超时,点击重新加载
                        mView?.loadMoreEnd(true)
                    }
                    KLog.i("doAddData page Ino $dataPageCache $pageInfo $t")
                }
                else -> {
                    super.doAddData(t)
                }
            }
        }
    }

    private fun getJumpIndex(page: Int): Int {
        var jumpIndex = 0
        if (dataPageCache.size > 0) {
            if (page > dataPageCache.lastKey()) {
                dataPageCache.forEach {
                    jumpIndex += it.value + 1
                }
                return jumpIndex
            }
            val pages = dataPageCache.firstKey()..dataPageCache.lastKey()
            if (page in pages) {
                val pre = if (!dataPageCache.keys.contains(page) && pageInfo.activePage > page) -1 else 0
                dataPageCache.forEach {
                    if (page > it.key) jumpIndex += it.value + 1
                }
                return jumpIndex + pre
            }
        }
        return jumpIndex
    }


    override fun onPresenterDestroyed() {
        super.onPresenterDestroyed()
        pageModeDisposable.dispose()
    }
}