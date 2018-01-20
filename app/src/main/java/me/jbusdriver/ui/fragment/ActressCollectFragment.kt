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
import io.reactivex.rxkotlin.subscribeBy
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_menu_op_head.view.*
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.common.*
import me.jbusdriver.db.bean.ActressCategory
import me.jbusdriver.db.service.CategoryService
import me.jbusdriver.mvp.ActressCollectContract
import me.jbusdriver.mvp.bean.ActressDBType
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.CollectLinkWrapper
import me.jbusdriver.mvp.presenter.ActressCollectPresenterImpl
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.adapter.BaseAppAdapter
import me.jbusdriver.ui.data.AppConfiguration
import me.jbusdriver.ui.data.collect.ActressCollector
import me.jbusdriver.ui.helper.CollectCategoryHelper
import me.jbusdriver.ui.holder.CollectDirEditHolder
import java.util.*


class ActressCollectFragment : AppBaseRecycleFragment<ActressCollectContract.ActressCollectPresenter, ActressCollectContract.ActressCollectView, CollectLinkWrapper<ActressInfo>>(), ActressCollectContract.ActressCollectView {


    override fun createPresenter() = ActressCollectPresenterImpl(ActressCollector)

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

                        GlideApp.with(holder.itemView.context).asBitmap().load(actress.avatar.toGlideUrl)
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
                val data = this@ActressCollectFragment.adapter.getData().getOrNull(position) ?: return@setOnItemClickListener
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
                (this@ActressCollectFragment.adapter.getData().getOrNull(position)?.linkBean)?.let {
                    MaterialDialog.Builder(viewContext)
                            .title(it.name)
                            .items(listOf("取消收藏"))
                            .itemsCallback { _, _, _, text ->
                                if (ActressCollector.removeCollect(it)) {
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

    private val dataHelper by lazy { CollectCategoryHelper<ActressInfo>() }

    private val holder by lazy { CollectDirEditHolder(viewContext) }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.findItem(R.id.action_collect_dir_edit)?.setOnMenuItemClickListener {
            holder.showDialogWithData(dataHelper.getCollectGroup().keys.toList()) { delActionsParams, addActionsParams ->
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
        val dd = data as List<ActressInfo>
        Flowable.just(dd).map {
            dataHelper.initFromData(dd, ActressCategory.id!!)
        }.compose(SchedulersCompat.io()).subscribeBy({
            KLog.e(it.message)
            it.printStackTrace()
        }, {
            val delegate = dataHelper.getDelegate()
            KLog.d("needInjectType : ${delegate.needInjectType}")
            delegate.needInjectType.onEach {
                if (it == -1) delegate.registerItemType(it, R.layout.layout_actress_item) //默认注入类型0，即actress
                else delegate.registerItemType(it, R.layout.layout_menu_op_head) //头部，可以做特化
            }

            adapter.setMultiTypeDelegate(delegate)
            adapter.addData(dataHelper.getDataWrapper())
            if (AppConfiguration.enableCategory) adapter.expand(0)
        }).addTo(rxManager)

    }


    companion object {
        fun newInstance() = ActressCollectFragment()
    }
}