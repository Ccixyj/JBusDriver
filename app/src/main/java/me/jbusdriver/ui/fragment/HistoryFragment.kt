package me.jbusdriver.ui.fragment

import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
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

    override val adapter: BaseQuickAdapter<History, in BaseViewHolder> by lazy {
        val format = SimpleDateFormat("yyyy-MM-dd")
        object : BaseQuickAdapter<History, BaseViewHolder>(R.layout.layout_history_item) {

            override fun convert(helper: BaseViewHolder, item: History) {
                helper.setText(R.id.tv_history_date, format.format(item.createTime))
                        .setText(R.id.tv_history_title, item.des)
                if (item.img != null && !TextUtils.isEmpty(item.img)) {
                    helper.setVisible(R.id.iv_history_icon, true)
                    Glide.with(mContext).load(item.img.toGlideUrl).asBitmap().into(BitmapImageViewTarget(helper.getView(R.id.iv_history_icon)))
                } else {
                    helper.setGone(R.id.iv_history_icon, false)
                }
            }
        }.apply {
            setOnItemClickListener { adapter, view, position ->
                data.getOrNull(position)?.moveto(view.context)
            }
        }
    }

    companion object {
        fun newInstance() = HistoryFragment()
    }

}