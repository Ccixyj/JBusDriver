package me.jbusdriver.common

import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import com.cfzx.mvp.view.BaseView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.jbusdriver.mvp.presenter.BasePresenter

/**
 * Created by Administrator on 2017/4/9.
 */
abstract class AppBaseRecycleFragment<P : BasePresenter.BaseRefreshLoadMorePresenter<V>, V : BaseView.BaseListWithRefreshView, M> : AppBaseFragment<P, V>(), BaseView.BaseListWithRefreshView {

    abstract val swipeView: SwipeRefreshLayout
    abstract val recycleView: RecyclerView
    abstract val layoutManager: RecyclerView.LayoutManager
    abstract val adapter: BaseQuickAdapter<M, in BaseViewHolder>

    override fun initWidget(rootView: View) {
        
    }

}