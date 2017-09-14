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
import me.jbusdriver.mvp.LinkCollectContract
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.bean.des
import me.jbusdriver.mvp.presenter.LinkCollectPresenterImpl

/**
 * Created by Administrator on 2017/7/17 0017.
 */
class LinkCollectFragment : AppBaseRecycleFragment<LinkCollectContract.LinkCollectPresenter, LinkCollectContract.LinkCollectView, ILink>(), LinkCollectContract.LinkCollectView {


    override fun createPresenter() = LinkCollectPresenterImpl()

    override val swipeView: SwipeRefreshLayout? by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager by lazy { LinearLayoutManager(viewContext) }


    override val adapter: BaseQuickAdapter<ILink, in BaseViewHolder> by lazy {
        object : BaseQuickAdapter<ILink, BaseViewHolder>(android.R.layout.simple_list_item_1) {
            override fun convert(helper: BaseViewHolder, item: ILink) {
                helper.setText(android.R.id.text1, item.des)
            }
        }
    }

    override val layoutId: Int = R.layout.layout_swipe_recycle

    companion object {
        fun newInstance() = LinkCollectFragment()
    }
}