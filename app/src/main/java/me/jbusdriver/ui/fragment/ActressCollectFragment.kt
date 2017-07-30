package me.jbusdriver.ui.fragment

import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.OrientationHelper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.common.AppBaseRecycleFragment
import me.jbusdriver.common.KLog
import me.jbusdriver.common.spanCount
import me.jbusdriver.common.toast
import me.jbusdriver.mvp.ActressCollectContract
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.presenter.ActressCollectPresenterImpl
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.adapter.ActressInfoAdapter
import me.jbusdriver.ui.data.CollectManager

/**
 * Created by Administrator on 2017/7/17 0017.
 */
class ActressCollectFragment : AppBaseRecycleFragment<ActressCollectContract.ActressCollectPresenter, ActressCollectContract.ActressCollectView, ActressInfo>(), ActressCollectContract.ActressCollectView {


    override fun createPresenter() = ActressCollectPresenterImpl()

    override val swipeView: SwipeRefreshLayout?  by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager  by lazy { StaggeredGridLayoutManager(viewContext.spanCount, OrientationHelper.VERTICAL) }
    override val adapter: BaseQuickAdapter<ActressInfo, in BaseViewHolder> by lazy {
        ActressInfoAdapter(rxManager).apply {
            setOnItemClickListener { adapter, view, position ->
                adapter.data.getOrNull(position)?.let {
                    KLog.d("item : $it")
                    if (it is ILink) {
                        MovieListActivity.start(viewContext, it)
                    }
                }

            }

            setOnItemLongClickListener { adapter, _, position ->
                this@ActressCollectFragment.adapter.getData().getOrNull(position)?.let {
                    MaterialDialog.Builder(viewContext)
                            .title(it.name)
                            .items(listOf("取消收藏"))
                            .itemsCallback { _, _, _, text ->
                                if (CollectManager.removeCollect(it)) {
                                    viewContext.toast("取消收藏成功")
                                    adapter.data.removeAt(position)
                                    adapter.notifyItemRemoved(position)
                                } else {
                                    viewContext.toast("已经取消了")
                                }

                            }
                            .show()
                }
                true
            }
        }
    }


    override val layoutId: Int = R.layout.layout_swipe_recycle

    companion object {
        fun newInstance() = ActressCollectFragment()
    }
}