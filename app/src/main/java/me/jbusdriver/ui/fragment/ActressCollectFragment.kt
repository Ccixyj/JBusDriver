package me.jbusdriver.ui.fragment

import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.graphics.Palette
import android.support.v7.widget.OrientationHelper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.text.TextUtils
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.util.MultiTypeDelegate
import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.common.*
import me.jbusdriver.db.bean.Category
import me.jbusdriver.db.bean.MovieCategory
import me.jbusdriver.mvp.ActressCollectContract
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.ActressWrapper
import me.jbusdriver.mvp.presenter.ActressCollectPresenterImpl
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.data.AppConfiguration
import me.jbusdriver.ui.data.collect.ActressCollector
import java.util.*

/**
 * Created by Administrator on 2017/7/17 0017.
 */
class ActressCollectFragment : AppBaseRecycleFragment<ActressCollectContract.ActressCollectPresenter, ActressCollectContract.ActressCollectView, ActressWrapper>(), ActressCollectContract.ActressCollectView {


    override fun createPresenter() = ActressCollectPresenterImpl(ActressCollector)

    override val swipeView: SwipeRefreshLayout? by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager by lazy {
        StaggeredGridLayoutManager(viewContext.spanCount, OrientationHelper.VERTICAL)
    }


    override val adapter: BaseQuickAdapter<ActressWrapper, in BaseViewHolder> by lazy {

        object : BaseMultiItemQuickAdapter<ActressWrapper, BaseViewHolder>(null) {
            private val random = Random()
            private fun randomNum(number: Int) = Math.abs(random.nextInt() % number)

            init {
                addItemType(0, R.layout.layout_actress_item)
                addItemType(1, R.layout.layout_menu_op_head)
            }

            override fun convert(holder: BaseViewHolder, item: ActressWrapper) {
                KLog.d("item $item")

                when (item.itemType) {
                    0 -> {
                        KLog.d("convert :$item")
                        val actress = requireNotNull(item.actressInfo)

                        GlideApp.with(holder.itemView.context).asBitmap().load(actress.avatar.toGlideUrl)
                                .error(R.drawable.ic_nowprinting).into(object : BitmapImageViewTarget(holder.getView(R.id.iv_actress_avatar)) {
                            override fun onResourceReady(resource: Bitmap?, transition: Transition<in Bitmap>?) {
                                resource?.let {
                                    Flowable.just(it).map {
                                        Palette.from(it).generate()
                                    }.compose(SchedulersCompat.io())
                                            .subscribeWith(object : SimpleSubscriber<Palette>() {
                                                override fun onNext(it: Palette) {
                                                    super.onNext(it)
                                                    val swatch = listOf(it.lightMutedSwatch, it.lightVibrantSwatch, it.vibrantSwatch, it.mutedSwatch).filterNotNull()
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
                        KLog.d("convert :$item")
                        setFullSpan(holder)
                        holder.setText(R.id.tv_nav_menu_name, item.subItems.first().actressInfo?.category?.name)
                        holder.itemView.setOnClickListener {
                            if (item.isExpanded) collapse(holder.adapterPosition) else expand(holder.adapterPosition)
                        }
                    }
                }

            }
        }.apply {
            setOnItemClickListener { adapter, _, position ->
                val data = this@ActressCollectFragment.adapter.getData().getOrNull(position)
                (data?.actressInfo ?: (data as? ActressInfo))?.let {
                    MovieListActivity.start(viewContext, it)
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


    override fun initWidget(rootView: View) {
        if (AppConfiguration.enableCategory) {
            recycleView.layoutManager = layoutManager
            adapter.setOnLoadMoreListener({ mBasePresenter?.onLoadMore() }, recycleView)
            adapter.openLoadAnimation {
                arrayOf(ObjectAnimator.ofFloat(it, "alpha", 0.0f, 1.0f),
                        ObjectAnimator.ofFloat(it, "translationY", 120f, 0f))
            }
            swipeView?.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimaryLight)
            swipeView?.setOnRefreshListener { mBasePresenter?.onRefresh() }
            recycleView.adapter = adapter
        } else {
            super.initWidget(rootView)
        }
    }

    override fun showContents(datas: List<*>?) {
        val dd = datas as List<ActressInfo>
        if (AppConfiguration.enableCategory) {
            Flowable.just(dd).map {
                it.groupBy { it.category }
            }.subscribeBy {
                KLog.d("showContents group  $it")
                adapter.addData(reloadAdapter(it))
                adapter.expand(0)
            }
        } else {

            adapter.addData(reloadAdapter(mapOf(MovieCategory to dd)))

        }
        //super.showContents(datas)
//        if (AppConfiguration.enableCategory) {
//            adapter.expandAll()
//        }
    }

    private fun reloadAdapter(it: Map<Category, List<ActressInfo>>): List<ActressWrapper> {
        val delegate = object : MultiTypeDelegate<ActressWrapper>() {
            override fun getItemType(t: ActressWrapper): Int = t.itemType
        }
        val newDts = mutableListOf<ActressWrapper>()
        delegate.registerItemType(0, R.layout.layout_actress_item) //默认注入类型０，即actress
        it.forEach {
            delegate.registerItemType(it.key.depth + 1, layouts[it.key.depth] ?: R.layout.layout_actress_item)
            if (AppConfiguration.enableCategory) {
                newDts.add(ActressWrapper().apply {
                    it.value.forEach { addSubItem(ActressWrapper(it)) }
                })
            } else {
                it.value.mapTo(newDts) { ActressWrapper(it) }
            }
        }
        KLog.d("reloadAdapter size ${newDts.size}, $newDts")
        return newDts
    }

    private val layouts by lazy {
        mapOf(0 to R.layout.layout_actress_item)
        mapOf(1 to R.layout.layout_menu_op_head)
    }

    companion object {
        fun newInstance() = ActressCollectFragment()
    }
}