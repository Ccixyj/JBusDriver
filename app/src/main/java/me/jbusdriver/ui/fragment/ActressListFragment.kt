package me.jbusdriver.ui.fragment

import android.os.Bundle
import android.support.v4.util.ArrayMap
import android.support.v7.widget.OrientationHelper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.base.*
import me.jbusdriver.base.common.C
import me.jbusdriver.base.CacheLoader
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.mvp.model.CollectModel
import me.jbusdriver.mvp.presenter.ActressLinkPresenterImpl
import me.jbusdriver.mvp.presenter.LinkAbsPresenterImpl
import me.jbusdriver.ui.activity.SearchResultActivity
import me.jbusdriver.ui.adapter.ActressInfoAdapter
import me.jbusdriver.ui.data.AppConfiguration
import me.jbusdriver.ui.data.enums.DataSourceType

class ActressListFragment : LinkableListFragment<ActressInfo>() {

    private val link by lazy {
        val link = arguments?.getSerializable(C.BundleKey.Key_1)  as? ILink
                ?: error("no link data ")
        KLog.i("link data : $link")
        link
    }

    override val type: DataSourceType by lazy {
        arguments?.getSerializable(ACTRESS_LIST_DATA_TYPE) as? DataSourceType ?: let {
            (arguments?.getSerializable(C.BundleKey.Key_1) as? ILink)?.let { link ->
                val path = link.link.urlPath
                KLog.d("link data urlPath :$path ")
                val type = when {
                    link.link.urlHost.endsWith("xyz") -> {
                        //xyz
                        when {
                            path.startsWith("genre") -> DataSourceType.GENRE
                            path.startsWith("star") -> DataSourceType.ACTRESSES
                            else -> DataSourceType.CENSORED
                        }

                    }
                    else -> {
                        when {
                            path.startsWith("uncensored") -> {
                                //无码

                                when {
                                    path.startsWith("uncensored/genre") -> DataSourceType.GENRE
                                    path.startsWith("uncensored/star") -> DataSourceType.ACTRESSES
                                    else -> DataSourceType.CENSORED
                                }
                            }
                            else -> {
                                //有码
                                when {
                                    path.startsWith("genre") -> DataSourceType.GENRE
                                    path.startsWith("star") -> DataSourceType.ACTRESSES
                                    else -> DataSourceType.CENSORED
                                }
                            }
                        }

                    }

                }
                KLog.d("link data type :$type ")
                type

            } ?: DataSourceType.CENSORED
        }
    }

    private val isSearch by lazy { link is SearchLink && activity != null && activity is SearchResultActivity }

    override val layoutManager: RecyclerView.LayoutManager  by lazy { StaggeredGridLayoutManager(viewContext.spanCount, OrientationHelper.VERTICAL) }
    override val adapter by lazy { ActressInfoAdapter(rxManager) }

    override fun createPresenter() = ActressLinkPresenterImpl(link)

    override fun initData() {
        if (isSearch) {
            RxBus.toFlowable(SearchWord::class.java).subscribeBy { sea ->
                (mBasePresenter as? LinkAbsPresenterImpl<*>)?.let {
                    (it.linkData as SearchLink).query = sea.query
                    it.onRefresh()
                }
            }.addTo(rxManager)
        }
    }

    private var collectMenu: MenuItem? = null
    private var removeCollectMenu: MenuItem? = null
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.findItem(R.id.action_show_all)?.isVisible = false
        if (isSearch) {
            val isCollect = CollectModel.has((link as SearchLink).convertDBItem())
            collectMenu = menu?.add(Menu.NONE, R.id.action_add_movie_collect, 10, "收藏")?.apply {
                setIcon(R.drawable.ic_star_border_white_24dp)
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                isVisible = !isCollect
            }
            removeCollectMenu = menu?.add(Menu.NONE, R.id.action_remove_movie_collect, 10, "取消收藏")?.apply {
                setIcon(R.drawable.ic_star_white_24dp)
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                isVisible = isCollect
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_add_movie_collect -> {
                //收藏
                KLog.d("收藏")
                CollectModel.addToCollectForCategory(link.convertDBItem()) {
                    collectMenu?.isVisible = false
                    removeCollectMenu?.isVisible = true
                }
            }
            R.id.action_remove_movie_collect -> {
                //取消收藏
                KLog.d("取消收藏")
                if (CollectModel.removeCollect(link.convertDBItem())) {
                    collectMenu?.isVisible = true
                    removeCollectMenu?.isVisible = false
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun gotoSearchResult(query: String) {
        (mBasePresenter as?  LinkAbsPresenterImpl<*>)?.let {
            if (isSearch) {
                viewContext.toast("新搜索 : $query")
                RxBus.post(SearchWord(query))
            } else {
                super.gotoSearchResult(query)
            }
        }
    }

    override val pageMode: Int = AppConfiguration.PageMode.Page


    companion object {

        const val ACTRESS_LIST_DATA_TYPE = "actress:list:data:type"

        //需要处理搜索的特殊情况
        fun newInstance(link: ILink) = ActressListFragment().apply {
            arguments = Bundle().apply {
                putSerializable(C.BundleKey.Key_1, link)
            }
        }

        fun newInstance(type: DataSourceType) = ActressListFragment().apply {
            val urls = CacheLoader.acache.getAsString(C.Cache.BUS_URLS)?.let { GSON.fromJson<ArrayMap<String, String>>(it) }
                    ?: arrayMapof()
            val url = urls[type.key] ?: JAVBusService.defaultFastUrl+"/actresses"
            arguments = Bundle().apply {
                /*
                *
                * object : ILink {
                    override val link: String = url
                }*/
                putSerializable(C.BundleKey.Key_1, PageLink(1, type.key, url))
                putSerializable(ACTRESS_LIST_DATA_TYPE, type)
            }
        }

    }

    override fun insertData(pos: Int, data: List<*>) {
        adapter.addData(pos, data as List<ActressInfo>)
    }

    override fun moveTo(pos: Int) {
        layoutManager.scrollToPosition(adapter.headerLayoutCount + pos)
    }


}