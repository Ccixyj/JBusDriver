package me.jbusdriver.mvp.presenter

import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import me.jbusdriver.common.KLog
import me.jbusdriver.common.SchedulersCompat
import me.jbusdriver.db.bean.*
import me.jbusdriver.db.service.CategoryService
import me.jbusdriver.db.service.LinkService
import me.jbusdriver.mvp.ActressCollectContract
import me.jbusdriver.mvp.BaseView
import me.jbusdriver.mvp.MovieCollectContract
import me.jbusdriver.mvp.bean.CollectLinkWrapper
import me.jbusdriver.mvp.bean.PageInfo
import me.jbusdriver.mvp.bean.hasNext
import me.jbusdriver.mvp.model.BaseModel
import me.jbusdriver.ui.data.AppConfiguration
import me.jbusdriver.ui.data.collect.ICollect
import org.jsoup.nodes.Document


abstract class BaseAbsCollectPresenter<V : BaseView.BaseListWithRefreshView, T : ICollectCategory>(private val collector: ICollect<T>) : AbstractRefreshLoadMorePresenterImpl<V, T>(), BasePresenter.BaseCollectPresenter<T> {


    protected open val pageSize = 20

    private val ancestor by lazy {
        when {
            this is MovieCollectContract.MovieCollectPresenter -> MovieCategory
            this is ActressCollectContract.ActressCollectPresenter -> ActressCategory
            else -> LinkCategory
        }
    }
    private val listData by lazy { collector.dataList.toMutableList() }
    private val pageNum
        get() = ((listData.size - 1) / pageSize) + 1

    override val collectGroupMap: MutableMap<Category, List<T>> = mutableMapOf()

    override val dataWrapperList: MutableList<CollectLinkWrapper<T>> = mutableListOf()

    override val adapterDelegate: BasePresenter.BaseCollectPresenter.CollectMultiTypeDelegate<T> = BasePresenter.BaseCollectPresenter.CollectMultiTypeDelegate()

    override fun loadData4Page(page: Int) {
        //查询所有的分类 //优化:先查20个
        mView?.showLoading()
        if (AppConfiguration.enableCategory) {
            //一次性加载完成
            Flowable.just(ancestor)
                    .filter { ancestor.id != null }
                    .flatMap { Flowable.fromIterable(CategoryService.queryCategoryTreeLike(it.id!!)) }
                    .map {
                        val parent = CollectLinkWrapper<T>(it).apply {
                            adapterDelegate.needInjectType.add(level)
                        }
                        val list = LinkService.queryByCategory(it)
                        val items = mutableListOf<T>()
                        list.forEach {
                            val mapValue = it.getLinkValue() as? T
                            if (mapValue != null) {
                                parent.addSubItem(CollectLinkWrapper(null, mapValue).apply {
                                    adapterDelegate.needInjectType.add(level)
                                })
                                items.add(mapValue)
                            }
                        }
                        collectGroupMap[it] = items
                        parent
                    }

                    .toList()
                    .doFinally { mView?.dismissLoading() }
                    .subscribeBy({
                        mView?.showError(it)
                    }, {
                        mView?.resetList()
                        mView?.showContents(it)
                        mView?.loadMoreComplete()
                        mView?.loadMoreEnd()

                    })
                    .addTo(rxManager)

        } else {

            val next = if (page < pageNum) page + 1 else pageNum
            pageInfo = pageInfo.copy(activePage = page, nextPage = next)
            Flowable.just(pageInfo).map {
                KLog.d("request page : $it")
                val start = (pageInfo.activePage - 1) * pageSize
                val nextSize = start + pageSize
                val end = if (nextSize <= listData.size) nextSize else listData.size
                listData.subList(start, end).map {
                    CollectLinkWrapper(null, it).apply {
                        adapterDelegate.needInjectType.add(level)
                    }
                }
            }.doFinally { mView?.dismissLoading() }
                    .compose(SchedulersCompat.io())
                    .subscribeBy({
                        mView?.showError(it)
                    }, {
                        if (!pageInfo.hasNext) mView?.loadMoreEnd()
                    }, {
                        mView?.showContents(it)
                        mView?.loadMoreComplete()
                    }).addTo(rxManager)
        }


    }

    override fun onRefresh() {
        pageInfo = PageInfo()
//        listData.clear()
//        collector.reload()
//        listData.addAll(collector.dataList)
        mView?.resetList()
        loadData4Page(1)
    }

    override val model: BaseModel<Int, Document>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun stringMap(str: Document): List<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}