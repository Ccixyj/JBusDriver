package me.jbusdriver.mvp.presenter

import com.cfzx.utils.CacheLoader
import io.reactivex.Flowable
import io.reactivex.rxkotlin.subscribeBy
import me.jbusdriver.common.KLog
import me.jbusdriver.common.RxBus
import me.jbusdriver.common.urlHost
import me.jbusdriver.common.urlPath
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.LinkListContract
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.bean.PageChangeEvent
import me.jbusdriver.mvp.bean.PageInfo
import me.jbusdriver.mvp.model.BaseModel
import me.jbusdriver.ui.data.Configuration
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Created by Administrator on 2017/5/10 0010.
 */
abstract class LinkAbsPresenterImpl<T>(val linkData: ILink) : AbstractRefreshLoadMorePresenterImpl<LinkListContract.LinkListView, T>(), LinkListContract.LinkListPresenter {

    protected var IsAll = false
    private val dataPageCache by lazy { sortedMapOf<Int, Int>() }
    private val pageModeDisposable = RxBus.toFlowable(PageChangeEvent::class.java)
            .subscribeBy(onNext = {
                KLog.d("PageChangeEvent $it")

                onRefresh()
                //清空dataPageCache
                dataPageCache.clear()
            })


    override fun loadAll(iaAll: Boolean) {
        IsAll = iaAll
        loadData4Page(1)
    }

    /*不需要*/
    override val model: BaseModel<Int, Document> = object : BaseModel<Int, Document> {
        override fun requestFor(t: Int) =
                (if (t == 1) linkData.link else "${linkData.link.urlHost}${linkData.link.urlPath}/$t").let {
                    KLog.i("fromCallable page $pageInfo requestFor : $it")
                    JAVBusService.INSTANCE.get(it, if (IsAll) "all" else null).map { Jsoup.parse(it) }
                }.doOnNext {
                    if (t == 1) CacheLoader.lru.put("${linkData.link}$IsAll", it.toString())
                }

        override fun requestFromCache(t: Int) = Flowable.concat(CacheLoader.justLru(linkData.link).map { Jsoup.parse(it) }, requestFor(t))
                .firstOrError().toFlowable()
    }


    override fun onRefresh() {
        dataPageCache.clear()
        CacheLoader.lru.remove(linkData.link)
        super.onRefresh()
    }

    override fun lazyLoad() {
        onFirstLoad()
    }

    override fun onFirstLoad() {
        super.onFirstLoad()
    }

    override fun jumpToPage(page: Int) {
        KLog.i("jumpToPage $page in ${dataPageCache.keys}")
        if (page >= 1) {
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


    override fun isPageGap(currentPage: Int): Boolean {
        return Configuration.pageMode == Configuration.PageMode.Page && currentPage > 2 &&
                !dataPageCache.containsKey(currentPage - 1)
    }

    override fun doAddData(t: List<T>) {
        when (mView?.pageMode) {
            Configuration.PageMode.Page -> {
                //需要判断数据

                if (dataPageCache.size > 0 && pageInfo.activePage < dataPageCache.lastKey()) {
                    //当前页属于插入页面
                    mView?.insertDatas(getJumpIndex(pageInfo.activePage), t)
                } else {
                    super.doAddData(t)
                }
                if (t.isNotEmpty()) {
                    dataPageCache.put(pageInfo.activePage, t.size - 1)//page item In list
                } else {
                    //超过最大页数重置
                    mView?.loadMoreEnd()
                    if (pageInfo.nextPage < pageInfo.activePage && pageInfo.pages.isNotEmpty()) {
                        pageInfo = pageInfo.copy(activePage = pageInfo.nextPage - 1, nextPage = pageInfo.nextPage)
                    }
                    KLog.d("reset page $pageInfo")
                }

            }
            else -> {
                super.doAddData(t)
            }
        }
    }

    @Synchronized
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

    override fun pageInfo(): PageInfo = pageInfo

    override fun onPresenterDestroyed() {
        super.onPresenterDestroyed()
        pageModeDisposable.dispose()
    }
}