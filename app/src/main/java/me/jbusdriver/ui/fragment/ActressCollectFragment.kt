package me.jbusdriver.ui.fragment

import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.OrientationHelper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.common.AppBaseRecycleFragment
import me.jbusdriver.common.KLog
import me.jbusdriver.common.scressWidth
import me.jbusdriver.mvp.ActressCollectContract
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.presenter.ActressCollectPresenterImpl
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.adapter.ActressInfoAdapter

/**
 * Created by Administrator on 2017/7/17 0017.
 */
class ActressCollectFragment : AppBaseRecycleFragment<ActressCollectContract.ActressCollectPresenter, ActressCollectContract.ActressCollectView, ActressInfo>(), ActressCollectContract.ActressCollectView {

    val span by lazy {
        with(viewContext.scressWidth) {
            if (this < 1080) 3
            else if (this < 1440) 4
            else 6
        }
    }

    override fun createPresenter() = ActressCollectPresenterImpl()

    override val swipeView: SwipeRefreshLayout?  by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager  by lazy { StaggeredGridLayoutManager(span, OrientationHelper.VERTICAL) }
    override val adapter: BaseQuickAdapter<ActressInfo, in BaseViewHolder> by lazy {
        ActressInfoAdapter(rxManager).apply {
            setOnItemClickListener { adapter, view, position ->
                adapter.data.getOrNull(position)?.let {
                    KLog.d("item : $it")
                    if (it is ILink){
                        MovieListActivity.start(viewContext, it )
                    }
                }

            }
        }
    }


    override val layoutId: Int = R.layout.layout_swipe_recycle

    companion object {
        fun newInstance() = ActressCollectFragment()
    }
}