package me.jbusdriver.ui.fragment

import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.common.*
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.mvp.model.CollectModel
import me.jbusdriver.ui.activity.MovieDetailActivity
import me.jbusdriver.ui.adapter.BaseMultiItemAppAdapter
import me.jbusdriver.ui.data.AppConfiguration
import me.jbusdriver.ui.data.contextMenu.LinkMenu
import me.jbusdriver.ui.data.enums.DataSourceType


abstract class AbsMovieListFragment : LinkableListFragment<Movie>() {

    override val type: DataSourceType by lazy {
        arguments?.getSerializable(C.BundleKey.Key_1) as? DataSourceType ?: let {
            (arguments?.getSerializable(C.BundleKey.Key_1) as? ILink)?.let { link ->
                if (link is Movie) link.type
                else {
                    val urls = CacheLoader.acache.getAsString(C.Cache.BUS_URLS)?.let { AppContext.gson.fromJson<Map<String, String>>(it) }
                            ?: arrayMapof()
                    val key = urls.filter { link.link.startsWith(it.value) }.values.sortedBy { it.length }.lastOrNull()
                            ?: DataSourceType.CENSORED.key
                    val ck = urls.filter { it.value == key }.keys.first()
                    val ds = DataSourceType.values().firstOrNull { it.key == ck }
                            ?: DataSourceType.CENSORED
                    if (link is ActressInfo) {
                        when (ds) {
                            DataSourceType.CENSORED -> DataSourceType.ACTRESSES
                            DataSourceType.UNCENSORED -> DataSourceType.UNCENSORED_ACTRESSES
                            DataSourceType.XYZ -> DataSourceType.XYZ_ACTRESSES
                            else -> ds
                        }
                    }
                }
                DataSourceType.CENSORED
            } ?: DataSourceType.CENSORED
        }
    }


    override val adapter: BaseQuickAdapter<Movie, in BaseViewHolder>  by lazy {
        object : BaseMultiItemAppAdapter<Movie, BaseViewHolder>(null) {

            init {
                addItemType(-1, R.layout.layout_pager_section_item)
                addItemType(0, R.layout.layout_page_line_movie_item)
            }

            private val padding by lazy { this@AbsMovieListFragment.viewContext.dpToPx(8f) }
            private val colors = listOf(0xff2195f3.toInt(), 0xff4caf50.toInt(), 0xffff0030.toInt()) //蓝,绿,红

            private val lp by lazy {
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, this@AbsMovieListFragment.viewContext.dpToPx(24f)).apply {
                    leftMargin = padding
                    gravity = Gravity.CENTER_VERTICAL
                }
            }

            override fun convert(holder: BaseViewHolder, item: Movie) {
                when (item.itemType) {
                    -1 -> {
                        holder.setText(R.id.tv_page_num, item.title)
                        val currentPage = item.title.toIntOrNull()
                        if (currentPage != null) {
                            holder.setGone(R.id.tv_load_prev, mBasePresenter?.isPrevPageLoaded(currentPage)
                                    ?: true)
                            holder.getView<View>(R.id.tv_load_prev)?.setOnClickListener {
                                mBasePresenter?.jumpToPage(currentPage - 1)
                            }
                        }
                    }

                    0 -> {
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


                        GlideApp.with(this@AbsMovieListFragment).load(item.imageUrl.toGlideUrl).placeholder(R.drawable.ic_place_holder)
                                .error(R.drawable.ic_place_holder).centerCrop().into(DrawableImageViewTarget(holder.getView(R.id.iv_movie_img)))


                        with(holder.getView<LinearLayout>(R.id.ll_movie_hot)) {
                            this.removeAllViews()
                            item.tags.mapIndexed { index, tag ->
                                (viewContext.inflate(R.layout.tv_movie_tag) as TextView).let {
                                    it.text = tag
                                    it.setPadding(padding, 0, padding, 0)
                                    (it.background as? GradientDrawable)?.setColor(colors.getOrNull(index % 3)
                                            ?: colors.first())
                                    it.layoutParams = lp
                                    this.addView(it)
                                }
                            }

                        }
                        holder.getView<View>(R.id.card_movie_item)?.let {
                            it.setOnClickListener {
                                MovieDetailActivity.start(viewContext, item)
                            }
                            it.setOnLongClickListener {
                                KLog.d("setOnItemLongClickListener $item")

                                val action = if (CollectModel.has(item.convertDBItem())) LinkMenu.movieActions.minus("收藏")
                                else LinkMenu.movieActions.minus("取消收藏")

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
                    when (it.itemType) {
                        -1 -> {
                            KLog.d("change page view")
                            val pages = it.imageUrl.split("#").mapNotNull { it.toIntOrNull() }
                            if (pages.isNotEmpty()) showPageDialog(PageInfo(activePage = it.title.toInt(), pages = pages))
                        }
                        else -> {

                        }
                    }
                }
            }


        }
    }

    private fun showPageDialog(info: PageInfo) {
        MaterialDialog.Builder(viewContext).title("跳转:").items(info.pages.map {
            "${if (it > info.activePage) " 👇 跳至" else if (it == info.activePage) " 👉 当前" else " 👆 跳至"} 第 $it 页"
        }).itemsCallback { _, _, position, _ ->
            info.pages.getOrNull(position)?.let {
                mBasePresenter?.jumpToPage(it)
                adapter.notifyLoadMoreToLoading()
            }
        }.neutralText("输入页码").onNeutral { dialog, _ ->
            showEditDialog(info)
            dialog.dismiss()
        }.show()
    }

    private fun showEditDialog(info: PageInfo) {
        MaterialDialog.Builder(viewContext).title("输入页码:")
                .input("输入跳转页码", null, false, { dialog, input ->
                    KLog.d("page $input")
                    input.toString().toIntOrNull()?.let {
                        if (it < 1) {
                            viewContext.toast("必须输入大于0的整数!")
                            return@input
                        }
                        mBasePresenter?.jumpToPage(it)
                        dialog.dismiss()
                    } ?: let {
                        viewContext.toast("必须输入数字!")
                    }
                })
                .autoDismiss(false)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .neutralText("选择页码").onNeutral { dialog, _ ->
            showPageDialog(info)
            dialog.dismiss()
        }.show()
    }


    override val pageMode: Int
        get() = AppConfiguration.pageMode


    override fun insertData(pos: Int, data: List<*>) {
        adapter.addData(pos, data as List<Movie>)
    }

    override fun moveTo(pos: Int) {
        layoutManager.scrollToPosition(adapter.getHeaderLayoutCount() + pos)
    }

    override fun toString(): String = "$type :" + super.toString()


}