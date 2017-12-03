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
import com.chad.library.adapter.base.util.MultiTypeDelegate
import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_menu_op_head.view.*
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.common.*
import me.jbusdriver.db.bean.Category
import me.jbusdriver.db.bean.MovieCategory
import me.jbusdriver.db.service.CategoryService
import me.jbusdriver.mvp.ActressCollectContract
import me.jbusdriver.mvp.bean.ActressDBType
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.ActressWrapper
import me.jbusdriver.mvp.presenter.ActressCollectPresenterImpl
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.data.AppConfiguration
import me.jbusdriver.ui.data.collect.ActressCollector
import me.jbusdriver.ui.holder.CollectDirEditHolder
import java.util.*


class ActressCollectFragment : AppBaseRecycleFragment<ActressCollectContract.ActressCollectPresenter, ActressCollectContract.ActressCollectView, ActressWrapper>(), ActressCollectContract.ActressCollectView {


    override fun createPresenter() = ActressCollectPresenterImpl(ActressCollector)

    override val swipeView: SwipeRefreshLayout? by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager by lazy {
        StaggeredGridLayoutManager(viewContext.spanCount, OrientationHelper.VERTICAL)
    }


    override val adapter: BaseQuickAdapter<ActressWrapper, in BaseViewHolder> by lazy {

        object : BaseQuickAdapter<ActressWrapper, BaseViewHolder>(null) {
            private val random = Random()
            private fun randomNum(number: Int) = Math.abs(random.nextInt() % number)

            override fun convert(holder: BaseViewHolder, item: ActressWrapper) {
                when (holder.itemViewType) {
                    -1 -> {
                        val actress = requireNotNull(item.actressInfo)

                        GlideApp.with(holder.itemView.context).asBitmap().load(actress.avatar.toGlideUrl)
                                .error(R.drawable.ic_nowprinting).into(object : BitmapImageViewTarget(holder.getView(R.id.iv_actress_avatar)) {
                            override fun onResourceReady(resource: Bitmap?, transition: Transition<in Bitmap>?) {
                                resource?.let {
                                    Flowable.just(it).map {
                                        Palette.from(it).generate()
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
                                }

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
                        holder.setText(R.id.tv_nav_menu_name, " ${if (item.isExpanded) "👇" else "👆"} " + item.category?.name)
                    }
                }

            }
        }.apply {
            setOnItemClickListener { _, view, position ->
                val data = this@ActressCollectFragment.adapter.getData().getOrNull(position) ?: return@setOnItemClickListener
                KLog.d("click data : ${data.isExpanded} ; ${adapter.getData().size} ${adapter.getData()}")
                data.actressInfo?.let {
                    MovieListActivity.start(viewContext, it)
                } ?: apply {
                    view.tv_nav_menu_name.text = " ${if (data.isExpanded) "👇" else "👆"} " + data.category?.name
                    if (data.isExpanded) collapse(adapter.getHeaderLayoutCount() + position) else expand(adapter.getHeaderLayoutCount() + position)
                    (layoutManager as StaggeredGridLayoutManager).invalidateSpanAssignments()
                }
            }


            setOnItemLongClickListener { adapter, _, position ->
                (this@ActressCollectFragment.adapter.getData().getOrNull(position)?.actressInfo)?.let {
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

    private val actGroupMap by lazy { mutableMapOf<Category, List<ActressInfo>>() }

    private val holder by lazy {
        CollectDirEditHolder(viewContext)
    }
    private val categoryService by lazy { CategoryService() }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.findItem(R.id.action_collect_dir_edit)?.setOnMenuItemClickListener {
            holder.showDialogWithData(actGroupMap.keys.toList()) { delActionsParams, addActionsParams ->
                KLog.d("$delActionsParams $addActionsParams")
                if (delActionsParams.isNotEmpty()) {
                    delActionsParams.forEach {
                        try {
                            categoryService.delete(it, ActressDBType)
                        } catch (e: Exception) {
                            viewContext.toast("不能删除默认分类")
                        }
                    }
                }

                if (addActionsParams.isNotEmpty()) {
                    addActionsParams.forEach {
                        categoryService.insert(it)
                    }
                }
                mBasePresenter?.onRefresh()
            }
            true
        }
    }


    override fun showContents(data: List<*>) {
        val dd = data as List<ActressInfo>
        actGroupMap.clear()
        if (AppConfiguration.enableCategory) {
            Flowable.just(dd).compose(SchedulersCompat.io()).map {
                actGroupMap.putAll(it.groupBy { it.category })
                //添加其他未使用分类
                val usedId = actGroupMap.keys.mapNotNull { it.id }
                categoryService.queryTreeByLike(2).filterNot { usedId.contains(it.id) }
                        .forEach {
                            actGroupMap.put(it, emptyList())
                        }
                actGroupMap
            }.subscribeBy {
                KLog.d("showContents group  $it")
                adapter.addData(reloadAdapterData(it))
                adapter.expand(0)
            }.addTo(rxManager)
        } else {
            adapter.addData(reloadAdapterData(mapOf(MovieCategory to dd)))
        }
        //super.showContents(data)
//        if (AppConfiguration.enableCategory) {
//            adapter.expandAll()
//        }
    }

    private fun reloadAdapterData(group: Map<Category, List<ActressInfo>>): List<ActressWrapper> {
        val delegate = object : MultiTypeDelegate<ActressWrapper>() {
            override fun getItemType(t: ActressWrapper): Int = t.level
        }
        val newDts = mutableListOf<ActressWrapper>()

        group.forEach {
            if (AppConfiguration.enableCategory) {
                newDts.add(ActressWrapper(it.key).apply {
                    it.value.forEach {
                        addSubItem(ActressWrapper(null, it).apply {
                            delegate.registerItemType(level, R.layout.layout_actress_item) //默认注入类型0，即actress
                        })
                    }
                    delegate.registerItemType(level, R.layout.layout_menu_op_head)
                })
            } else {
                it.value.mapTo(newDts) { ActressWrapper(null, it) }
            }
        }
        //设置 delegate
        adapter.setMultiTypeDelegate(delegate)
        KLog.d("reloadAdapterData size ${newDts.size}, $newDts")
        return newDts
    }

    companion object {
        fun newInstance() = ActressCollectFragment()
    }
}