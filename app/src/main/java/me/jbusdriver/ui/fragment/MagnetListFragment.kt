package me.jbusdriver.ui.fragment

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.common.*
import me.jbusdriver.mvp.MagnetListContract
import me.jbusdriver.mvp.bean.Magnet
import me.jbusdriver.mvp.presenter.MagnetListPresenterImpl

class MagnetListFragment : AppBaseRecycleFragment<MagnetListContract.MagnetListPresenter, MagnetListContract.MagnetListView, Magnet>(), MagnetListContract.MagnetListView {

    private val keyword by lazy { arguments.getString(C.BundleKey.Key_1) ?: error("need keyword") }
    private val magnetLoaderKey by lazy { arguments.getString(C.BundleKey.Key_2) ?: error("need magnet loaderKey") }
    override fun createPresenter() = MagnetListPresenterImpl(magnetLoaderKey, keyword)

    override val layoutId: Int = R.layout.layout_swipe_recycle
    override val swipeView: SwipeRefreshLayout?  by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager  by lazy { LinearLayoutManager(viewContext) }


    override val adapter: BaseQuickAdapter<Magnet, in BaseViewHolder> by lazy {

        object : BaseQuickAdapter<Magnet, BaseViewHolder>(R.layout.layout_magnet_item) {

            override fun convert(helper: BaseViewHolder, item: Magnet) {
                KLog.d("convert $item")
                helper.setText(R.id.tv_magnet_title, item.name)
                        .setText(R.id.tv_magnet_date, item.date)
                        .setText(R.id.tv_magnet_size, item.size)
                        .addOnClickListener(R.id.iv_magnet_copy)
            }

        }.apply {
            emptyView = TextView(viewContext).apply {
                text = "没有种子数据"
                layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, viewContext.dpToPx(36f)).apply {
                    gravity = Gravity.CENTER
                }
            }

            setOnItemClickListener { adapter, _, position ->
                (adapter.data.getOrNull(position) as? Magnet)?.let { magnet ->
                    KLog.d("setOnItemClickListener $magnet")
                    viewContext.browse(magnet.link)
                }

            }

            setOnItemChildClickListener { adapter, view, position ->
                (adapter.data.getOrNull(position) as? Magnet)?.let { magnet ->
                    when (view.id) {
                        R.id.iv_magnet_copy -> {
                            KLog.d("copy $magnet")
                            view.context.apply {
                                copy(magnet.link)
                                toast("复制成功")
                            }

                        }
                        else -> Unit

                    }

                }
            }
        }

    }

    companion object {
        fun newInstance(keyword: String, loaderKey: String) = MagnetListFragment().apply {
            arguments = Bundle().apply {
                putString(C.BundleKey.Key_1, keyword)
                putString(C.BundleKey.Key_2, loaderKey)
            }
        }
    }

}