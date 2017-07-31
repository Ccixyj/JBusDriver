package me.jbusdriver.ui.fragment

import android.os.Bundle
import android.support.v4.util.ArrayMap
import android.support.v7.widget.OrientationHelper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.cfzx.utils.CacheLoader
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.common.*
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.presenter.ActressLinkPresenterImpl
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.adapter.ActressInfoAdapter
import me.jbusdriver.ui.data.DataSourceType

class ActressListFragment : LinkListFragment<ActressInfo>() {
    val link by lazy { arguments.getSerializable(C.BundleKey.Key_1)  as? ILink ?: error("no link data ") }

    override val layoutManager: RecyclerView.LayoutManager  by lazy { StaggeredGridLayoutManager(viewContext.spanCount, OrientationHelper.VERTICAL) }
    override val adapter by lazy { ActressInfoAdapter(rxManager).apply {
        setOnItemClickListener { adapter, _, position ->
            (adapter.data.getOrNull(position) as? ActressInfo)?.let {
                MovieListActivity.start(viewContext, it)
            }
        }

    } }
    override fun createPresenter() = ActressLinkPresenterImpl(link)

    override fun initWidget(rootView: View) {
        super.initWidget(rootView)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.findItem(R.id.action_show_all)?.isVisible = false
    }

    companion object {
        fun newInstance(link: ILink) = ActressListFragment().apply {
            arguments = Bundle().apply {
                putSerializable(C.BundleKey.Key_1, link)
            }
        }

        fun newInstance(type: DataSourceType) = ActressListFragment().apply {
            val urls = CacheLoader.acache.getAsString(C.Cache.BUS_URLS)?.let { AppContext.gson.fromJson<ArrayMap<String, String>>(it) } ?: arrayMapof()
            val url = urls[type.key] ?: JAVBusService.defaultFastUrl + "/actresses"
            arguments = Bundle().apply {
                putSerializable(C.BundleKey.Key_1, object : ILink {
                    override val link: String = url
                })
            }
        }

    }

}