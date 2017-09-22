package me.jbusdriver.ui.fragment

import android.os.Bundle
import android.support.v4.util.ArrayMap
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.common.AppBaseRecycleFragment
import me.jbusdriver.common.toGlideUrl
import me.jbusdriver.db.bean.History
import me.jbusdriver.mvp.HistoryContract
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.mvp.presenter.HistoryPresenterImpl
import java.text.SimpleDateFormat

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
        menu.add(Menu.NONE, R.id.cancel_action, 10, "清除历史记录").apply {
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

        object : BaseQuickAdapter<History, BaseViewHolder>(R.layout.layout_history_item) {
            val format = SimpleDateFormat("yyyy-MM-dd")
            val linkCache by lazy { ArrayMap<Int, ILink>() }
            override fun convert(helper: BaseViewHolder, item: History) {
                val itemLink = linkCache.getOrPut(item.hashCode()) { item.getLinkItem() }
                val appender = if (itemLink !is Movie && itemLink !is SearchLink) {
                    if (item.isAll) "全部电影" else "已有种子电影"
                } else ""
                helper.setText(R.id.tv_history_date, format.format(item.createTime))
                        .setText(R.id.tv_history_title, itemLink.des + appender)

                val img by lazy {
                    if (itemLink is ActressInfo) {
                        itemLink.avatar
                    } else (itemLink as? Movie)?.imageUrl ?: ""
                }

                if (img.isNotBlank()) {
                    helper.setVisible(R.id.iv_history_icon, true)
                    Glide.with(mContext).load(img.toGlideUrl).asBitmap().into(BitmapImageViewTarget(helper.getView(R.id.iv_history_icon)))
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