package me.jbusdriver.mvp.presenter

import com.afollestad.materialdialogs.MaterialDialog
import com.cfzx.utils.CacheLoader
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import me.jbusdriver.common.*
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.LinkListContract
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.bean.PageInfo
import me.jbusdriver.mvp.model.BaseModel
import me.jbusdriver.ui.data.Configuration
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.HttpException

/**
 * Created by Administrator on 2017/5/10 0010.
 */
abstract class LinkAbsPresenterImpl<T>(val linkData: ILink) : AbstractRefreshLoadMorePresenterImpl<LinkListContract.LinkListView, T>(), LinkListContract.LinkListPresenter {

    protected var IsAll = false
    private val dataPageCache by lazy { sortedMapOf<Int, Int>() }
    private val pageModeDisposable = RxBus.toFlowable(Configuration.PageChangeEvent::class.java)
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

                val request = (if (page == 1) model.requestFromCache(page)
                else model.requestFor(page))
                request.onErrorResumeNext(Function {
                    return@Function when {
                        (it is HttpException && it.code() == 404) -> Flowable.just(Jsoup.parse(""))
                        else -> throw  it
                    }
                }).map { doc ->
                    parsePage(doc)?.let {
                        pageInfo = it
                        stringMap(doc)
                    } ?: listOf()
                }.compose(SchedulersCompat.io())
                        .subscribeWith(JumpSubscriber(page))
                        .addTo(rxManager)
            }
        } else {
            KLog.w("can't jump to page $page")
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

    override fun doAddData(t: List<T>) {
        dataPageCache.put(pageInfo.activePage, t.size - 1)//page item In list
        super.doAddData(t)
    }

    inner class JumpSubscriber(override val pageIndex: Int) : ListDefaultSubscriber(pageIndex) {

        var loading: MaterialDialog? = null

        override fun onStart() {
            AndroidSchedulers.mainThread().scheduleDirect {
                loading = mView?.viewContext?.let { MaterialDialog.Builder(it).progress(true, 0).content("正在加载...").show() }
            }
        }

        override fun onComplete() {
            loading?.dismiss()
            if (pageIndex != pageInfo.activePage) {
                KLog.w("page $pageIndex is mess : $pageInfo")
                pageInfo = pageInfo.copy(activePage = pageIndex)
            }
        }

        override fun onError(e: Throwable) {
            pageInfo = PageInfo(pageIndex - 1, pageIndex)
            mView?.viewContext?.toast("加载第${pageIndex}页失败")
        }

        override fun onNext(t: List<T>) {
            if (t.isNotEmpty()){
                when (mView?.pageMode) {
                    Configuration.PageMode.Page -> {
                        //需要判断数据
                        if (dataPageCache.size > 0 && pageInfo.activePage < dataPageCache.lastKey()) {
                            //当前页属于插入页面
                            mView?.insertDatas(getJumpIndex(pageInfo.activePage), t)
                        } else {
                            mView?.showContents(t)
                            if (pageInfo.activePage > 1) mView?.loadMoreComplete()
                        }
                        dataPageCache.put(pageInfo.activePage, t.size - 1)//page item In list
                    }
                    else -> {
                        mView?.showContents(t)
                        if (pageInfo.activePage > 1) mView?.loadMoreComplete()
                    }
                }
            } else mView?.viewContext?.toast("没有数据")
        }
    }
}