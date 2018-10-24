package me.jbusdriver.ui.fragment

import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_menu_op_head.view.*
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.base.GlideApp
import me.jbusdriver.base.KLog
import me.jbusdriver.base.common.AppBaseRecycleFragment
import me.jbusdriver.base.toast
import me.jbusdriver.base.glide.toGlideNoHostUrl
import me.jbusdriver.db.bean.Category
import me.jbusdriver.db.bean.MovieCategory
import me.jbusdriver.db.service.CategoryService
import me.jbusdriver.mvp.MovieCollectContract
import me.jbusdriver.mvp.bean.CollectLinkWrapper
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.bean.MovieDBType
import me.jbusdriver.mvp.bean.convertDBItem
import me.jbusdriver.mvp.model.CollectModel
import me.jbusdriver.mvp.presenter.MovieCollectPresenterImpl
import me.jbusdriver.ui.activity.MovieDetailActivity
import me.jbusdriver.ui.adapter.BaseAppAdapter
import me.jbusdriver.ui.data.AppConfiguration
import me.jbusdriver.ui.data.contextMenu.LinkMenu
import me.jbusdriver.ui.holder.CollectDirEditHolder

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

                        val movie = requireNotNull(item.linkBean)

                        holder.setText(R.id.tv_movie_title, movie.title)
                                .setText(R.id.tv_movie_date, movie.date)
                                .setText(R.id.tv_movie_code, movie.code)

                        GlideApp.with(viewContext).load(movie.imageUrl.toGlideNoHostUrl).placeholder(R.drawable.ic_place_holder)
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
                val data = this@MovieCollectFragment.adapter.getData().getOrNull(position)
                        ?: return@setOnItemClickListener
                KLog.d("click data : ${data.isExpanded} ; ${adapter.getData().size} ${adapter.getData()}")
                data.linkBean?.let {
                    MovieDetailActivity.start(viewContext, it)
                } ?: apply {
                    view.tv_nav_menu_name.text = " ${if (data.isExpanded) "👇" else "👆"} " + data.category.name
                    if (data.isExpanded) collapse(adapter.getHeaderLayoutCount() + position) else expand(adapter.getHeaderLayoutCount() + position)
                }
            }

            setOnItemLongClickListener { adapter, _, position ->
                (this@MovieCollectFragment.adapter.getData().getOrNull(position)?.linkBean)?.let { movie ->
                    val action = LinkMenu.movieActions.toMutableMap()
                    if (AppConfiguration.enableCategory) {
                        val category = CategoryService.getById(movie.categoryId)
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

                    action.remove("收藏")
                    action["取消收藏"] = {
                        if (CollectModel.removeCollect(it.convertDBItem())) {
                            viewContext.toast("取消收藏成功")
                            adapter.data.removeAt(position)
                            adapter.notifyItemRemoved(position)
                        } else {
                            viewContext.toast("已经取消了")
                        }
                    }

                    MaterialDialog.Builder(viewContext).title(movie.code)
                            .content(movie.title)
                            .items(action.keys)
                            .itemsCallback { _, _, _, text ->
                                action[text]?.invoke(movie)
                            }
                            .show()

                }
                true
            }


        }
    }


    private val holder by lazy { CollectDirEditHolder(viewContext, MovieCategory) }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.findItem(R.id.action_collect_dir_edit)?.setOnMenuItemClickListener {

            holder.showDialogWithData(mBasePresenter?.collectGroupMap?.keys?.toList()
                    ?: emptyList()) { delActionsParams, addActionsParams ->
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

    override fun createPresenter() = MovieCollectPresenterImpl()


    override fun showContents(data: List<*>) {
        KLog.d("showContents $data")
        mBasePresenter?.let { p ->
            p.adapterDelegate.needInjectType.onEach {
                if (it == -1) p.adapterDelegate.registerItemType(it, R.layout.layout_movie_item) //默认注入类型0，即actress
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
        fun newInstance() = MovieCollectFragment()
    }
}