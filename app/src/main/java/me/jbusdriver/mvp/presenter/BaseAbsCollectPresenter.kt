package me.jbusdriver.mvp.presenter

import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.base.KLog
import me.jbusdriver.base.SchedulersCompat
import me.jbusdriver.base.mvp.BaseView
import me.jbusdriver.base.mvp.model.BaseModel
import me.jbusdriver.db.bean.*
import me.jbusdriver.db.service.CategoryService
import me.jbusdriver.db.service.LinkService
import me.jbusdriver.mvp.ActressCollectContract
import me.jbusdriver.mvp.MovieCollectContract
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.mvp.model.CollectModel
import me.jbusdriver.ui.data.AppConfiguration
import org.jsoup.nodes.Document


abstract class BaseAbsCollectPresenter<V : BaseView.BaseListWithRefreshView, T : ICollectCategory> : AbstractRefreshLoadMorePresenterImpl<V, T>(),BaseCollectPresenter<T> {


    protected open val pageSize = 20

    private val ancestor by lazy {
        when {
            this is MovieCollectContract.MovieCollectPresenter -> MovieCategory
            this is ActressCollectContract.ActressCollectPresenter -> ActressCategory
            else -> LinkCategory
        }
    }
    private val listData by lazy {
        load().toMutableList()
    }

    private val pageNum
        get() = ((listData.size - 1) / pageSize) + 1

    override val collectGroupMap: MutableMap<Category, List<T>> = mutableMapOf()

    override val adapterDelegate: BaseCollectPresenter.CollectMultiTypeDelegate<T> = BaseCollectPresenter.CollectMultiTypeDelegate()


    private fun load() = when {
        this is MovieCollectContract.MovieCollectPresenter -> LinkService.queryMovies()
        this is ActressCollectContract.ActressCollectPresenter -> LinkService.queryActress()
        else -> LinkService.queryLink()
    }

    override fun loadData4Page(page: Int) {
        //查询所有的分类 //优化:先查20个

        if (AppConfiguration.enableCategory) {
            //一次性加载完成
            Flowable.just(ancestor)
                    .filter { ancestor.id != null }
                    .flatMap { Flowable.fromIterable(CategoryService.queryCategoryTreeLike(it.id!!)) }
                    .map { cate ->
                        val parent = CollectLinkWrapper<T>(cate).apply {
                            adapterDelegate.needInjectType.add(level)
                        }
                        val list = LinkService.queryByCategory(cate)
                        val items = mutableListOf<T>()
                        list.forEach {
                            val mapValue = it.getLinkValue() as? T
                            if (mapValue != null) {
                                parent.addSubItem(CollectLinkWrapper(cate, mapValue).apply {
                                    adapterDelegate.needInjectType.add(level)
                                })
                                items.add(mapValue)
                            }
                        }
                        collectGroupMap[cate] = items
                        parent
                    }

                    .toList()
                    .doOnSubscribe { mView?.showLoading() }
                    .doAfterTerminate { mView?.dismissLoading() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
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
                    CollectLinkWrapper(null, it.convertDBItem().getLinkValue()).apply {
                        adapterDelegate.needInjectType.add(level)
                    }
                }
            }.doOnSubscribe { mView?.showLoading() }
                    .doAfterTerminate { mView?.dismissLoading() }
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
        //   pageInfo = PageInfo()
//        listData.clear()
//        collector.reload()
//        listData.addAll(collector.dataList)
        if (!AppConfiguration.enableCategory) {
            listData.clear()
            listData.addAll(load())
        } else {
            collectGroupMap.clear()
        }

        mView?.resetList()
        loadData4Page(1)
    }

    override val model: BaseModel<Int, Document>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun stringMap(page: PageInfo, str: Document): List<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun setCategory(t: T, category: Category) {
        require(t is ILink && category.id != null)
        val dbItem = (t as ILink).convertDBItem().apply { categoryId = category.id!! }
        CollectModel.update(dbItem)
    }
}