package me.jbusdriver.ui.fragment

import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.common.KLog
import me.jbusdriver.common.dpToPx
import me.jbusdriver.common.toGlideUrl
import me.jbusdriver.common.toast
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.bean.PageInfo
import me.jbusdriver.ui.activity.MovieDetailActivity
import me.jbusdriver.ui.data.Configuration


abstract class AbsMovieListFragment : LinkableListFragment<Movie>() {

    override val adapter: BaseQuickAdapter<Movie, in BaseViewHolder>  by lazy {
        object : BaseMultiItemQuickAdapter<Movie, BaseViewHolder>(null) {

            init {
                addItemType(-1, R.layout.layout_pager_section_item)
                addItemType(0, R.layout.layout_page_line_movie_item)
            }

            val padding by lazy { this@AbsMovieListFragment.viewContext.dpToPx(8f) }
            val colors = listOf(0xff2195f3.toInt(), 0xff4caf50.toInt(), 0xffff0030.toInt()) //蓝,绿,红

            val lp by lazy {
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
                            holder.setGone(R.id.tv_load_prev, mBasePresenter?.isPageGap(currentPage) ?: true)
                            holder.getView<View>(R.id.tv_load_prev)?.setOnClickListener {
                                mBasePresenter?.jumpToPage(currentPage - 1)
                            }
                        }
                    }

                    0 -> {
                        when (pageMode) {
                            Configuration.PageMode.Page -> {
                                holder.setGone(R.id.v_line, true)
                            }
                            Configuration.PageMode.Normal -> {
                                holder.setGone(R.id.v_line, false)
                            }
                        }


                        holder.setText(R.id.tv_movie_title, item.title)
                                .setText(R.id.tv_movie_date, item.date)
                                .setText(R.id.tv_movie_code, item.code)

                        Glide.with(this@AbsMovieListFragment).load(item.imageUrl.toGlideUrl).placeholder(R.drawable.ic_place_holder)
                                .error(R.drawable.ic_place_holder).centerCrop().into(GlideDrawableImageViewTarget(holder.getView(R.id.iv_movie_img)))


                        with(holder.getView<LinearLayout>(R.id.ll_movie_hot)) {
                            this.removeAllViews()
                            item.tags.mapIndexed { index, tag ->
                                (mLayoutInflater.inflate(R.layout.tv_movie_tag, null) as TextView).let {
                                    it.text = tag
                                    it.setPadding(padding, 0, padding, 0)
                                    (it.background as? GradientDrawable)?.setColor(colors.getOrNull(index % 3) ?: colors.first())
                                    it.layoutParams = lp
                                    this.addView(it)
                                }
                            }

                        }
                        holder.getView<View>(R.id.card_movie_item)?.setOnClickListener {
                            MovieDetailActivity.start(activity, item)
                        }

                        holder.addOnLongClickListener(R.id.card_movie_item)
                    }
                }
            }
        }.apply {
            setOnItemClickListener { adapter, _, position ->
                (adapter.data.getOrNull(position) as? Movie)?.let {
                    when (it.itemType) {
                        -1 -> {
                            KLog.d("change page view")
                            mBasePresenter?.pageInfo()?.let {
                                if (it.pages.isNotEmpty()) showPageDialog(it)
                            }
                        }
                        0 -> {
//                        MovieDetailActivity.start(activity, it)
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
            "${if (it > info.activePage) "后跳至" else if (it == info.activePage) "当前" else "前跳至"} 第 $it 页"
        }).itemsCallback { _, _, position, _ ->
            info.pages.getOrNull(position)?.let {
                mBasePresenter?.jumpToPage(it)
                adapter.notifyLoadMoreToLoading()
            }
        }.neutralText("输入页码").onNeutral { dialog, _ ->
            showEditDialog()
            dialog.dismiss()
        }.show()
    }

    private fun showEditDialog() {
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
                        //
                        viewContext.toast("必须输入数字!")
                    }
                })
                .autoDismiss(false)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .neutralText("选择页码").onNeutral { dialog, _ ->
            mBasePresenter?.pageInfo()?.let {
                if (it.pages.isNotEmpty()) showPageDialog(it)
            }
            dialog.dismiss()
        }.show()
    }


    override val pageMode: Int
        get() = Configuration.pageMode


    override fun insertDatas(pos: Int, datas: List<*>) {
        KLog.d("insertDatas to $pos : $datas")
        adapter.addData(pos, datas as List<Movie>)
    }

    override fun moveTo(pos: Int) {
        KLog.d("move to $pos")
        layoutManager.scrollToPosition(adapter.getHeaderLayoutCount() + pos)
    }
}