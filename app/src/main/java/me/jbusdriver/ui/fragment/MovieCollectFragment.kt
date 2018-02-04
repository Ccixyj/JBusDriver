package me.jbusdriver.ui.fragment

import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_menu_op_head.view.*
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.common.*
import me.jbusdriver.db.bean.MovieCategory
import me.jbusdriver.db.service.CategoryService
import me.jbusdriver.mvp.MovieCollectContract
import me.jbusdriver.mvp.bean.CollectLinkWrapper
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.bean.MovieDBType
import me.jbusdriver.mvp.presenter.MovieCollectPresenterImpl
import me.jbusdriver.ui.activity.MovieDetailActivity
import me.jbusdriver.ui.adapter.BaseAppAdapter
import me.jbusdriver.ui.data.collect.MovieCollector
import me.jbusdriver.ui.helper.CollectCategoryHelper
import me.jbusdriver.ui.holder.CollectDirEditHolder

/**
 * Created by Administrator on 2017/7/17 0017.
 */
class MovieCollectFragment : AppBaseRecycleFragment<MovieCollectContract.MovieCollectPresenter, MovieCollectContract.MovieCollectView, CollectLinkWrapper<Movie>>(), MovieCollectContract.MovieCollectView {

    override val swipeView: SwipeRefreshLayout? by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager by lazy { LinearLayoutManager(viewContext) }
    override val layoutId: Int = R.layout.layout_swipe_recycle
    override val adapter: BaseQuickAdapter<CollectLinkWrapper<Movie>, in BaseViewHolder> by lazy {
        object : BaseAppAdapter<CollectLinkWrapper<Movie>, BaseViewHolder>(null) {

            override fun convert(holder: BaseViewHolder, item: CollectLinkWrapper<Movie>) {
                when (holder.itemViewType) {
                    -1 -> {

                        val movie =  requireNotNull(item.linkBean)

                        holder.setText(R.id.tv_movie_title, movie.title)
                                .setText(R.id.tv_movie_date, movie.date)
                                .setText(R.id.tv_movie_code, movie.code)

                        GlideApp.with(viewContext).load(movie.imageUrl.toGlideUrl).placeholder(R.drawable.ic_place_holder)
                                .error(R.drawable.ic_place_holder).centerCrop().into(DrawableImageViewTarget(holder.getView(R.id.iv_movie_img)))

                        holder.getView<View>(R.id.card_movie_item)?.setOnClickListener {
                            MovieDetailActivity.start(viewContext, movie)
                        }

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
                val data = this@MovieCollectFragment.adapter.getData().getOrNull(position) ?: return@setOnItemClickListener
                KLog.d("click data : ${data.isExpanded} ; ${adapter.getData().size} ${adapter.getData()}")
                data.linkBean?.let {
                    MovieDetailActivity.start(viewContext, it)
                } ?: apply {
                    view.tv_nav_menu_name.text = " ${if (data.isExpanded) "👇" else "👆"} " + data.category.name
                    if (data.isExpanded) collapse(adapter.getHeaderLayoutCount() + position) else expand(adapter.getHeaderLayoutCount() + position)
                }
            }

        }
    }


    private val dataHelper by lazy { CollectCategoryHelper<Movie>() }
    private val holder by lazy { CollectDirEditHolder(viewContext, MovieCategory) }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.findItem(R.id.action_collect_dir_edit)?.setOnMenuItemClickListener {

            holder.showDialogWithData(mBasePresenter?.collectGroupMap?.keys?.toList() ?: emptyList()) { delActionsParams, addActionsParams ->
                KLog.d("$delActionsParams $addActionsParams")
                if (delActionsParams.isNotEmpty()) {
                    delActionsParams.forEach {
                        try {
                            CategoryService.delete(it, MovieDBType)
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

    override fun createPresenter() = MovieCollectPresenterImpl(MovieCollector)


    override fun showContents(data: List<*>) {
        KLog.d("showContents $data")
        mBasePresenter?.let { p ->
            p.adapterDelegate.needInjectType.onEach {
                if (it == -1)  p.adapterDelegate.registerItemType(it, R.layout.layout_movie_item) //默认注入类型0，即actress
                else  p.adapterDelegate.registerItemType(it, R.layout.layout_menu_op_head) //头部，可以做特化
            }
            adapter.setMultiTypeDelegate(p.adapterDelegate)
        }

        super.showContents(data)
        adapter.expand(0)
    }

    companion object {
        fun newInstance() = MovieCollectFragment()
    }
}