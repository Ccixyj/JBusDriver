package me.jbusdriver.ui.fragment

import android.os.Bundle
import android.support.v4.util.ArrayMap
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.base.GlideApp
import me.jbusdriver.base.common.AppBaseRecycleFragment
import me.jbusdriver.base.glide.toGlideNoHostUrl
import me.jbusdriver.db.bean.History
import me.jbusdriver.mvp.HistoryContract
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.mvp.presenter.HistoryPresenterImpl
import me.jbusdriver.ui.adapter.BaseAppAdapter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Administrator on 2017/9/18 0018.
 */
class HistoryFragment : AppBaseRecycleFragment<HistoryContract.HistoryPresenter, HistoryContract.HistoryView, History>(), HistoryContract.HistoryView {

    override fun createPresenter() = HistoryPresenterImpl()

    override val layoutId: Int = R.layout.layout_swipe_recycle
    override val swipeView: SwipeRefreshLayout?  by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager  by lazy { LinearLayoutManager(viewContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.add(Menu.NONE, Menu.NONE, 10, "清除历史记录").apply {
            setIcon(R.drawable.ic_delete_24dp)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener {
                mBasePresenter?.clearHistory()
                adapter.setNewData(null)
                adapter.notifyDataSetChanged()
                true
            }

        }

    }

    override val adapter: BaseQuickAdapter<History, in BaseViewHolder> by lazy {


        object : BaseAppAdapter<History, BaseViewHolder>(R.layout.layout_history_item) {

            val linkCache by lazy { ArrayMap<Int, ILink>() }
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

            override fun convert(helper: BaseViewHolder, item: History) {
                val itemLink = linkCache.getOrPut(item.hashCode()) { item.getLinkItem() }
                val appender = if (itemLink !is Movie && itemLink !is SearchLink) {
                    if (item.isAll) "全部电影" else "已有种子电影"
                } else ""
                helper.setText(R.id.tv_history_date, format.format(item.createTime))
                        .setText(R.id.tv_history_title, itemLink.des + appender)

                val img by lazy {
                    (itemLink as? ActressInfo)?.avatar ?: (itemLink as? Movie)?.imageUrl ?: ""
                }

                if (img.isNotBlank()) {
                    helper.setVisible(R.id.iv_history_icon, true)
                    GlideApp.with(mContext).load(img.toGlideNoHostUrl).into(helper.getView(R.id.iv_history_icon))
                } else {
                    helper.setGone(R.id.iv_history_icon, false)
                }
            }

        }.apply {
            setOnItemClickListener { _, view, position ->
                data.getOrNull(position)?.move(view.context)
            }
        }

    }

    companion object {
        fun newInstance() = HistoryFragment()
    }

}