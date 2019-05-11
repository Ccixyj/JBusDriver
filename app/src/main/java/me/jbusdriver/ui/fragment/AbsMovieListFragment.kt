package me.jbusdriver.ui.fragment

import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.util.MultiTypeDelegate
import me.jbusdriver.R
import me.jbusdriver.base.*
import me.jbusdriver.base.common.C
import me.jbusdriver.common.bean.ILink
import me.jbusdriver.common.isEndWithXyzHost
import me.jbusdriver.common.toGlideNoHostUrl
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.bean.convertDBItem
import me.jbusdriver.mvp.model.CollectModel
import me.jbusdriver.ui.activity.MovieDetailActivity
import me.jbusdriver.ui.data.AppConfiguration
import me.jbusdriver.ui.data.contextMenu.LinkMenu
import me.jbusdriver.ui.data.enums.DataSourceType


abstract class AbsMovieListFragment : LinkableListFragment<Movie>() {

    /*
        CENSORED("有碼", "/page/"), //有码

    GENRE("有碼類別"), //类别

    ACTRESSES("有碼女優"), //女优

     */
    override val type: DataSourceType by lazy {
        arguments?.getSerializable(MOVIE_LIST_DATA_TYPE) as? DataSourceType ?: let {
            (arguments?.getSerializable(C.BundleKey.Key_1) as? ILink)?.let { link ->

                val path = link.link.urlPath
                val type = when {
                    link.link.urlHost.isEndWithXyzHost -> {
                        //xyz
                        when {
                            path.startsWith("genre") -> DataSourceType.GENRE
                            path.startsWith("star") -> DataSourceType.ACTRESSES
                            else -> DataSourceType.CENSORED
                        }

                    }
                    else -> {
                        when {
                            path.startsWith("uncensored") -> {
                                //无码

                                when {
                                    path.startsWith("uncensored/genre") -> DataSourceType.GENRE
                                    path.startsWith("uncensored/star") -> DataSourceType.ACTRESSES
                                    else -> DataSourceType.CENSORED
                                }
                            }
                            else -> {
                                //有码
                                when {
                                    path.startsWith("genre") -> DataSourceType.GENRE
                                    path.startsWith("star") -> DataSourceType.ACTRESSES
                                    else -> DataSourceType.CENSORED
                                }
                            }
                        }

                    }

                }
                type

            } ?: DataSourceType.CENSORED
        }
    }


    override val adapter: BaseQuickAdapter<Movie, in BaseViewHolder>  by lazy {
        object : BaseQuickAdapter<Movie, BaseViewHolder>(null) {

            private val Movie.isInValid
                inline get() = TextUtils.isEmpty(code) && TextUtils.isEmpty(link)

            init {

                multiTypeDelegate = object : MultiTypeDelegate<Movie>() {
                    override fun getItemType(t: Movie): Int {
                        return when {
                            t.isInValid -> -1
                            recyclerView?.layoutManager is LinearLayoutManager -> OrientationHelper.VERTICAL
                            recyclerView?.layoutManager is StaggeredGridLayoutManager -> OrientationHelper.HORIZONTAL
                            else -> 1
                        }
                    }
                }

                multiTypeDelegate
                    .registerItemType(-1, R.layout.layout_pager_section_item)
                    .registerItemType(OrientationHelper.VERTICAL, R.layout.layout_page_line_movie_item)
                    .registerItemType(OrientationHelper.HORIZONTAL, R.layout.layout_page_line_movie_item_hor)

            }

            private val dp8 by lazy { this@AbsMovieListFragment.viewContext.dpToPx(8f) }
            private val backColors = listOf(0xff2195f3.toInt(), 0xff4caf50.toInt(), 0xffff0030.toInt()) //蓝,绿,红

            private fun genLp() = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                this@AbsMovieListFragment.viewContext.dpToPx(24f)
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
            }

            override fun convert(holder: BaseViewHolder, item: Movie) {
                when (holder.itemViewType) {
                    -1 -> {
                        setFullSpan(holder)
                        holder.setText(R.id.tv_page_num, item.title)
                        val currentPage = item.title.toIntOrNull()
                        if (currentPage != null) {
                            holder.setGone(
                                R.id.tv_load_prev, mBasePresenter?.isPrevPageLoaded(currentPage)
                                    ?: true
                            )
                            holder.getView<View>(R.id.tv_load_prev)?.setOnClickListener {
                                mBasePresenter?.jumpToPage(currentPage - 1)
                            }
                        }
                    }
                    OrientationHelper.HORIZONTAL, OrientationHelper.VERTICAL -> {

                        when (pageMode) {
                            AppConfiguration.PageMode.Page -> {
                                holder.setGone(R.id.v_line, true)
                            }
                            AppConfiguration.PageMode.Normal -> {
                                holder.setGone(R.id.v_line, false)
                            }
                        }


                        holder.setText(R.id.tv_movie_title, item.title)
                            .setText(R.id.tv_movie_date, item.date)
                            .setText(R.id.tv_movie_code, item.code)


                        GlideApp.with(this@AbsMovieListFragment).load(item.imageUrl.toGlideNoHostUrl)
                            .placeholder(R.drawable.ic_place_holder)
                            .error(R.drawable.ic_place_holder).centerCrop()
                            .into(DrawableImageViewTarget(holder.getView(R.id.iv_movie_img)))


                        with(holder.getView<LinearLayout>(R.id.ll_movie_hot)) {
                            this.removeAllViews()
                            item.tags?.mapIndexed { index, tag ->
                                (viewContext.inflate(R.layout.tv_movie_tag) as TextView).let {
                                    it.text = tag
                                    if (holder.itemViewType == OrientationHelper.HORIZONTAL) {
                                        it.textSize = 11f
                                    }
                                    it.setPadding(dp8, 0, dp8, 0)
                                    it.background = GradientDrawable().apply {
                                        setColor(
                                            backColors.getOrNull(index % 3)
                                                ?: backColors.first()
                                        )
                                        cornerRadius = if (holder.itemViewType == OrientationHelper.HORIZONTAL) {
                                            dp8 * 1.5f
                                        } else {
                                            dp8 * 2f
                                        }
                                    }
                                    it.layoutParams = genLp().apply {
                                        if (holder.itemViewType == OrientationHelper.VERTICAL) {
                                            leftMargin = dp8
                                        } else {
                                            rightMargin = dp8
                                        }
                                    }
                                    this.addView(it)
                                }
                            }

                        }
                        holder.getView<View>(R.id.card_movie_item)?.let {
                            it.setOnClickListener {
                                MovieDetailActivity.start(viewContext, item)
                            }
                            it.setOnLongClickListener {

                                val action =
                                    (if (CollectModel.has(item.convertDBItem())) LinkMenu.movieActions.minus("收藏")
                                    else LinkMenu.movieActions.minus("取消收藏")).toMutableMap()
                                if (AppConfiguration.enableCategory) {
                                    val ac = action.remove("收藏")
                                    if (ac != null) {
                                        action["收藏到分类..."] = ac
                                    }
                                }
                                MaterialDialog.Builder(viewContext).title(item.code)
                                    .content(item.title)
                                    .items(action.keys)
                                    .itemsCallback { _, _, _, text ->
                                        action[text]?.invoke(item)
                                    }
                                    .show()
                                return@setOnLongClickListener true
                            }

                        }

                    }

                }


            }
        }.apply {
            setOnItemClickListener { adapter, _, position ->
                (adapter.data.getOrNull(position) as? Movie)?.let {
                    when (adapter.multiTypeDelegate?.getDefItemViewType(adapter.data, position)) {
                        -1 -> {
                            mBasePresenter?.currentPageInfo?.let {
                                if (it.referPages.isNotEmpty()) showPageDialog(it)
                            }

                        }
                        else -> {

                        }
                    }
                }
            }


        }
    }


    override fun insertData(pos: Int, data: List<*>) {
        adapter.addData(pos, data as List<Movie>)
    }

    override fun moveTo(pos: Int) {
        layoutManager.scrollToPosition(adapter.getHeaderLayoutCount() + pos)
    }


    //    override fun toString(): String = "$type :" + super.toString()

    companion object {
        const val MOVIE_LIST_DATA_TYPE = "movie:list:data:type"
    }

}