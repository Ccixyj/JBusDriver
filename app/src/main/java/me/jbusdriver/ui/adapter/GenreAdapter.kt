package me.jbusdriver.ui.adapter

import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.common.AppContext
import me.jbusdriver.common.KLog
import me.jbusdriver.common.copy
import me.jbusdriver.common.toast
import me.jbusdriver.mvp.bean.Genre
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.data.CollectManager

/**
 * Created by Administrator on 2017/7/30.
 */
open class GenreAdapter : BaseQuickAdapter<Genre, BaseViewHolder>(R.layout.layout_genre_item) {

    private val actionMap by lazy {
        mapOf("复制" to { genre: Genre ->
            AppContext.instace.copy(genre.name)
            AppContext.instace.toast("已复制")
        }, "收藏" to { header ->
            CollectManager.addToCollect(header)
            KLog.d("link data ${CollectManager.linkCache}")
        }, "取消收藏" to { header ->
            CollectManager.removeCollect(header)
            KLog.d("link data ${CollectManager.linkCache}")
        })
    }

    override fun convert(holder: BaseViewHolder, item: Genre) {
        holder.setText(R.id.tv_movie_genre, item.name)
        (holder.getView<TextView>(R.id.tv_movie_genre).background as? GradientDrawable)?.apply {
            setColor(holder.itemView.resources.getColor(R.color.colorPrimary))
        }
    }

    init {


        setOnItemClickListener { _, view, position ->
            data.getOrNull(position)?.let { genre ->
                KLog.d("genre : $genre")
                MovieListActivity.start(view.context, genre)
            }
        }

        setOnItemLongClickListener { adapter, _, position ->
            (adapter.data.getOrNull(position) as? Genre)?.let { item ->
                val action = if (CollectManager.has(item as ILink)) actionMap.minus("收藏")
                else actionMap.minus("取消收藏")

                MaterialDialog.Builder(this.mContext).content(item.name)
                        .items(action.keys)
                        .itemsCallback { _, _, _, text ->
                            action[text]?.invoke(item)
                        }.show()
            }
            true
        }
    }
}