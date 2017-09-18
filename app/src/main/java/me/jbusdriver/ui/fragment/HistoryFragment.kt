package me.jbusdriver.ui.fragment

import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.common.AppBaseRecycleFragment
import me.jbusdriver.mvp.HistoryContract
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.presenter.HistoryPresenterImpl

/**
 * Created by Administrator on 2017/9/18 0018.
 */
class HistoryFragment : AppBaseRecycleFragment<HistoryContract.HistoryPresenter, HistoryContract.HistoryView, ILink>(), HistoryContract.HistoryView {

    override fun createPresenter() = HistoryPresenterImpl()

    override val layoutId: Int = R.layout.layout_swipe_recycle
    override val swipeView: SwipeRefreshLayout?  by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager  by lazy { LinearLayoutManager(viewContext) }

    override val adapter: BaseQuickAdapter<ILink, in BaseViewHolder> by lazy {
        error("")
    }

}