package me.jbusdriver.ui.fragment

import android.graphics.Bitmap
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.graphics.Palette
import android.support.v7.widget.OrientationHelper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_menu_op_head.view.*
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.base.*
import me.jbusdriver.base.common.AppBaseRecycleFragment
import me.jbusdriver.base.glide.toGlideNoHostUrl
import me.jbusdriver.db.bean.ActressCategory
import me.jbusdriver.db.bean.Category
import me.jbusdriver.db.service.CategoryService
import me.jbusdriver.mvp.ActressCollectContract
import me.jbusdriver.mvp.bean.ActressDBType
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.CollectLinkWrapper
import me.jbusdriver.mvp.bean.convertDBItem
import me.jbusdriver.mvp.model.CollectModel
import me.jbusdriver.mvp.presenter.ActressCollectPresenterImpl
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.adapter.BaseAppAdapter
import me.jbusdriver.ui.data.AppConfiguration
import me.jbusdriver.ui.data.contextMenu.LinkMenu
import me.jbusdriver.ui.holder.CollectDirEditHolder
import java.util.*


class ActressCollectFragment : AppBaseRecycleFragment<ActressCollectContract.ActressCollectPresenter, ActressCollectContract.ActressCollectView, CollectLinkWrapper<ActressInfo>>(), ActressCollectContract.ActressCollectView {


    override val swipeView: SwipeRefreshLayout? by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager by lazy {
        StaggeredGridLayoutManager(viewContext.spanCount, OrientationHelper.VERTICAL)
    }
    override val adapter: BaseQuickAdapter<CollectLinkWrapper<ActressInfo>, in BaseViewHolder> by lazy {

        object : BaseAppAdapter<CollectLinkWrapper<ActressInfo>, BaseViewHolder>(null) {
            private val random = Random()
            private fun randomNum(number: Int) = Math.abs(random.nextInt() % number)

            override fun convert(holder: BaseViewHolder, item: CollectLinkWrapper<ActressInfo>) {
                when (holder.itemViewType) {
                    -1 -> {
                        val actress = requireNotNull(item.linkBean)

                        GlideApp.with(holder.itemView.context).asBitmap().load(actress.avatar.toGlideNoHostUrl)
                                .error(R.drawable.ic_nowprinting).into(object : BitmapImageViewTarget(holder.getView(R.id.iv_actress_avatar)) {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                Flowable.just(resource).map {
                                    Palette.from(resource).generate()
                                }.compose(SchedulersCompat.io())
                                        .subscribeWith(object : SimpleSubscriber<Palette>() {
                                            override fun onNext(t: Palette) {
                                                super.onNext(t)
                                                val swatch = listOfNotNull(t.lightMutedSwatch, t.lightVibrantSwatch, t.vibrantSwatch, t.mutedSwatch)
                                                if (!swatch.isEmpty()) {
                                                    swatch[randomNum(swatch.size)].let {
                                                        holder.setBackgroundColor(R.id.tv_actress_name, it.rgb)
                                                        holder.setTextColor(R.id.tv_actress_name, it.bodyTextColor)
                                                    }
                                                }
                                            }
                                        })
                                        .addTo(rxManager)

                                super.onResourceReady(resource, transition)
                            }
                        })
                        //加载名字
                        holder.setText(R.id.tv_actress_name, actress.name)

                        holder.setText(R.id.tv_actress_tag, actress.tag)
                        holder.setVisible(R.id.tv_actress_tag, !TextUtils.isEmpty(actress.tag))
                    }

                    else -> {
                        KLog.d("type item $item")
                        setFullSpan(holder)
                        holder.setText(R.id.tv_nav_menu_name, " ${if (item.isExpanded) "👇" else "👆"} " + item.category.name)
                    }
                }

            }
        }.apply {
            setOnItemClickListener { _, view, position ->
                val data = this@ActressCollectFragment.adapter.getData().getOrNull(position)
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
                (this@ActressCollectFragment.adapter.getData().getOrNull(position)?.linkBean)?.let { act ->
                    val action = LinkMenu.actressActions.toMutableMap()
                    action.remove("收藏")
                    if (AppConfiguration.enableCategory) {
                        val category = CategoryService.getById(act.categoryId)
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

                    MaterialDialog.Builder(viewContext).title(act.name)
                            .items(action.keys)
                            .itemsCallback { _, _, _, text ->
                                action[text]?.invoke(act)
                            }
                            .show()

                }
                true
            }
        }

    }
    override val layoutId: Int = R.layout.layout_swipe_recycle


    private val holder by lazy { CollectDirEditHolder(viewContext, ActressCategory) }

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

    override fun initData() {
        adapter.setMultiTypeDelegate(mBasePresenter?.adapterDelegate)
    }

    override fun createPresenter() = ActressCollectPresenterImpl()

    override fun showContents(data: List<*>) {
        KLog.d("showContents $data")

        mBasePresenter?.let { p ->
            p.adapterDelegate.needInjectType.onEach {
                if (it == -1) p.adapterDelegate.registerItemType(it, R.layout.layout_actress_item) //默认注入类型0，即actress
                else p.adapterDelegate.registerItemType(it, R.layout.layout_menu_op_head) //头部，可以做特化
            }
            p.adapterDelegate.needInjectType.clear()
        }
        super.showContents(data)
        if (AppConfiguration.enableCategory) {
            adapter.expand(0)
        }
    }

    companion object {
        fun newInstance() = ActressCollectFragment()
    }
}