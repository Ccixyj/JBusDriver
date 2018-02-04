package me.jbusdriver.ui.fragment

import android.graphics.Paint
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.Menu
import android.view.MenuInflater
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_menu_op_head.view.*
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.common.AppBaseRecycleFragment
import me.jbusdriver.common.KLog
import me.jbusdriver.common.dpToPx
import me.jbusdriver.common.toast
import me.jbusdriver.db.bean.LinkCategory
import me.jbusdriver.db.service.CategoryService
import me.jbusdriver.mvp.LinkCollectContract
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.mvp.presenter.LinkCollectPresenterImpl
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.activity.SearchResultActivity
import me.jbusdriver.ui.adapter.BaseAppAdapter
import me.jbusdriver.ui.data.collect.LinkCollector
import me.jbusdriver.ui.holder.CollectDirEditHolder

class LinkCollectFragment : AppBaseRecycleFragment<LinkCollectContract.LinkCollectPresenter, LinkCollectContract.LinkCollectView, CollectLinkWrapper<ILink>>(), LinkCollectContract.LinkCollectView {

    override val swipeView: SwipeRefreshLayout? by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager by lazy { LinearLayoutManager(viewContext) }
    override val adapter: BaseQuickAdapter<CollectLinkWrapper<ILink>, in BaseViewHolder> by lazy {
        object : BaseAppAdapter<CollectLinkWrapper<ILink>, BaseViewHolder>(R.layout.layout_header_item) {

            override fun convert(holder: BaseViewHolder, collect: CollectLinkWrapper<ILink>) {
                 when(holder.itemViewType){
                     -1->{
                         val item =requireNotNull(collect.linkBean)
                         val des = item.des.split(" ")
                         KLog.d("des ${des.joinToString(",")}")
                         holder.getView<TextView>(R.id.tv_head_value)?.apply {
                             setTextColor(ResourcesCompat.getColor(this@apply.resources, R.color.colorPrimaryDark, null))
                             paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG

                             setOnClickListener {
                                 KLog.d("setOnClickListener text : $item")
                                 if (item is SearchLink){
                                     SearchResultActivity.start(mContext,item.query)
                                     return@setOnClickListener
                                 }
                                 MovieListActivity.start(mContext, item)
                             }

                             //长按操作
                             setOnLongClickListener {
                                 KLog.d("setOnLongClickListener text : $item")

                                 MaterialDialog.Builder(holder.itemView.context).content(item.des)
                                         .items(listOf("取消收藏"))
                                         .itemsCallback { _, _, _, text ->
                                             LinkCollector.removeCollect(item)
                                             mData.removeAt(holder.adapterPosition)
                                             adapter.notifyItemRemoved(holder.adapterPosition)
                                         }.show()
                                 return@setOnLongClickListener true

                             }
                         }
                         val dp8 = mContext.dpToPx(8f)
                         holder.itemView.setPadding(dp8 * 2, dp8, dp8 * 2, dp8)
                         holder.setText(R.id.tv_head_name, des.firstOrNull())
                                 .setText(R.id.tv_head_value, des.lastOrNull())

                     }
                     else -> {
                         KLog.d("type item $collect")
                         setFullSpan(holder)
                         holder.setText(R.id.tv_nav_menu_name, " ${if (collect.isExpanded) "👇" else "👆"} " + collect.category.name)
                     }
                 }
            }
        }.apply {
            setOnItemClickListener { _, view, position ->
                val data = this@LinkCollectFragment.adapter.getData().getOrNull(position)
                        ?: return@setOnItemClickListener
                KLog.d("click data : ${data.isExpanded} ; ${adapter.getData().size} ${adapter.getData()}")
                data.linkBean?.let {
                    MovieListActivity.start(viewContext, it)
                } ?: apply {
                    view.tv_nav_menu_name.text = " ${if (data.isExpanded) "👇" else "👆"} " + data.category.name
                    if (data.isExpanded) collapse(adapter.getHeaderLayoutCount() + position) else expand(adapter.getHeaderLayoutCount() + position)
                    (layoutManager as StaggeredGridLayoutManager).invalidateSpanAssignments()
                }
            }

            setOnItemLongClickListener { adapter, _, position ->

                true
            }


        }
    }
    override val layoutId: Int = R.layout.layout_swipe_recycle

    override fun createPresenter() = LinkCollectPresenterImpl(LinkCollector)
    private val holder by lazy { CollectDirEditHolder(viewContext, LinkCategory) }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.findItem(R.id.action_collect_dir_edit)?.setOnMenuItemClickListener {

            holder.showDialogWithData(mBasePresenter?.collectGroupMap?.keys?.toList()
                    ?: emptyList()) { delActionsParams, addActionsParams ->
                KLog.d("$delActionsParams $addActionsParams")
                if (delActionsParams.isNotEmpty()) {
                    delActionsParams.forEach {
                        try {
                            CategoryService.delete(it, ActressDBType)
                        } catch (e: Exception) {
                            viewContext.toast("不能删除默认分类")
                        }
                    }
                }

                if (addActionsParams.isNotEmpty()) {
                    addActionsParams.forEach {
                        CategoryService.insert(it)
                    }
                }
                mBasePresenter?.onRefresh()
            }
            true
        }
    }



    override fun showContents(data: List<*>) {
        KLog.d("showContents $data")
        mBasePresenter?.let { p ->
            p.adapterDelegate.needInjectType.onEach {
                if (it == -1) p.adapterDelegate.registerItemType(it, R.layout.layout_actress_item) //默认注入类型0，即actress
                else p.adapterDelegate.registerItemType(it, R.layout.layout_menu_op_head) //头部，可以做特化
            }
            adapter.setMultiTypeDelegate(p.adapterDelegate)
        }

        super.showContents(data)
        adapter.expand(0)
    }


    companion object {
        fun newInstance() = LinkCollectFragment()
    }
}