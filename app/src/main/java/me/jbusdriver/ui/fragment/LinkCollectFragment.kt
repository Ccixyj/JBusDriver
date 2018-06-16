package me.jbusdriver.ui.fragment

import android.graphics.Paint
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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
import me.jbusdriver.base.KLog
import me.jbusdriver.base.dpToPx
import me.jbusdriver.base.toast
import me.jbusdriver.base.common.AppBaseRecycleFragment
import me.jbusdriver.db.bean.Category
import me.jbusdriver.db.bean.LinkCategory
import me.jbusdriver.db.service.CategoryService
import me.jbusdriver.mvp.LinkCollectContract
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.mvp.model.CollectModel
import me.jbusdriver.mvp.presenter.LinkCollectPresenterImpl
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.activity.SearchResultActivity
import me.jbusdriver.ui.adapter.BaseAppAdapter
import me.jbusdriver.ui.data.AppConfiguration
import me.jbusdriver.ui.data.contextMenu.LinkMenu
import me.jbusdriver.ui.holder.CollectDirEditHolder

class LinkCollectFragment : AppBaseRecycleFragment<LinkCollectContract.LinkCollectPresenter, LinkCollectContract.LinkCollectView, CollectLinkWrapper<ILink>>(), LinkCollectContract.LinkCollectView {

    override val swipeView: SwipeRefreshLayout? by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager by lazy { LinearLayoutManager(viewContext) }
    override val adapter: BaseQuickAdapter<CollectLinkWrapper<ILink>, in BaseViewHolder> by lazy {
        object : BaseAppAdapter<CollectLinkWrapper<ILink>, BaseViewHolder>(null) {

            override fun convert(holder: BaseViewHolder, collect: CollectLinkWrapper<ILink>) {
                when (holder.itemViewType) {
                    -1 -> {
                        val item = requireNotNull(collect.linkBean)
                        val des = item.des.split(" ")
                        KLog.d("des ${des.joinToString(",")}")
                        holder.getView<TextView>(R.id.tv_head_value)?.apply {
                            setTextColor(ResourcesCompat.getColor(this@apply.resources, R.color.colorPrimaryDark, null))
                            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG

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
                    if (it is SearchLink) {
                        SearchResultActivity.start(viewContext, it.query)
                    } else MovieListActivity.start(viewContext, it)

                } ?: apply {
                    view.tv_nav_menu_name.text = " ${if (data.isExpanded) "👇" else "👆"} " + data.category.name
                    if (data.isExpanded) collapse(adapter.getHeaderLayoutCount() + position) else expand(adapter.getHeaderLayoutCount() + position)
                }
            }

            setOnItemLongClickListener { adapter, _, position ->
                (this@LinkCollectFragment.adapter.getData().getOrNull(position)?.linkBean)?.let { link ->
                    val action = LinkMenu.linkActions.toMutableMap()
                    action.remove("收藏")
                    if (AppConfiguration.enableCategory) {
                        val category = CategoryService.getById(link.categoryId)
                        if (category != null) {
                            val all = mBasePresenter?.collectGroupMap?.keys ?: emptyList<Category>()
                            val last = all - category
                            if (last.isNotEmpty()) {
                                action.put("移到分类...") { link ->
                                    KLog.d("移到分类 : $last")
                                    MaterialDialog.Builder(viewContext).title("选择目录")
                                            .items(last.map { it.name })
                                            .itemsCallbackSingleChoice(-1) { _, _, w, _ ->
                                                KLog.d("选择 : $w")
                                                last.getOrNull(w)?.let {
                                                    mBasePresenter?.setCategory(link, it)
                                                    mBasePresenter?.onRefresh()
                                                }
                                                return@itemsCallbackSingleChoice true
                                            }.show()
                                }
                            }
                        }
                    }

                    action["取消收藏"] = {
                        if (CollectModel.removeCollect(it.convertDBItem())) {
                            viewContext.toast("取消收藏成功")
                            adapter.data.removeAt(position)
                            adapter.notifyItemRemoved(position)
                        } else {
                            viewContext.toast("已经取消了")
                        }
                    }
                    MaterialDialog.Builder(viewContext).content(link.des)
                            .items(action.keys)
                            .itemsCallback { _, _, _, text ->
                                action[text]?.invoke(link)
                            }.show()

                }
                true
            }


        }
    }
    override val layoutId: Int = R.layout.layout_swipe_recycle

    override fun createPresenter() = LinkCollectPresenterImpl()
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
                            CategoryService.delete(it, 3) //link 数据库中默认为3 具体可以有3..6
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
                if (it == -1) p.adapterDelegate.registerItemType(it, R.layout.layout_header_item) //默认注入类型0，即actress
                else p.adapterDelegate.registerItemType(it, R.layout.layout_menu_op_head) //头部，可以做特化
            }
            adapter.setMultiTypeDelegate(p.adapterDelegate)
        }

        super.showContents(data)
        if (AppConfiguration.enableCategory) {
            adapter.expand(0)
        }
    }


    companion object {
        fun newInstance() = LinkCollectFragment()
    }
}