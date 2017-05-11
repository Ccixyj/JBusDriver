package me.jbusdriver.ui.fragment

import android.os.Bundle
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_actress_attr.view.*
import me.jbusdriver.common.C
import me.jbusdriver.common.KLog
import me.jbusdriver.common.inflate
import me.jbusdriver.mvp.MovieListContract
import me.jbusdriver.mvp.bean.ActressAttrs
import me.jbusdriver.mvp.bean.IAttr
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.presenter.SimpleMovieListPresenter
import me.jbusdriver.ui.data.DataSourceType


/**
 * Created by Administraor on 2017/4/9.
 */
class SimpleMovieListFragment : MovieListFragment(), MovieListContract.MovieListView {


    val link by lazy { arguments.getSerializable(C.BundleKey.Key_2)  as? ILink ?: error("no link data ") }


    override fun createPresenter() = SimpleMovieListPresenter(link)
    /*================================================*/

    companion object {
        fun newInstance(type: DataSourceType, link: ILink) = SimpleMovieListFragment().apply {
            arguments = Bundle().apply {
                putSerializable(C.BundleKey.Key_1, type)
                putSerializable(C.BundleKey.Key_2, link)
            }
        }
    }

    fun addMovieAttr(data: IAttr) {
        when (data) {
            is ActressAttrs -> {
                adapter.removeAllHeaderView()
                adapter.addHeaderView(this.viewContext.inflate(R.layout.layout_actress_attr).apply {
                    //img
                    Glide.with(this@SimpleMovieListFragment).load(data.imageUrl).into(GlideDrawableImageViewTarget(this.iv_actress_avatar))
                    //title
                    this.ll_attr_container.addView(generateTextView().apply {
                        textSize = 16f
                        setTextColor(resources.getColor(R.color.primaryText))
                        text = data.title
                    })

                    data.info.forEach {
                        this.ll_attr_container.addView(generateTextView().apply { text = it })
                    }
                })
            }
        }
    }

    private fun generateTextView() = TextView(this.viewContext).apply {
        textSize = 11.5f
        setTextColor(resources.getColor(R.color.secondText))
    }

    override fun <T> showContent(data: T?) {
        KLog.d("parse res :$data")
        if (data is IAttr) {
            addMovieAttr(data)
        }
    }

}