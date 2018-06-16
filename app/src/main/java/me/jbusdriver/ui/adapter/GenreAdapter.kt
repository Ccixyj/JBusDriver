package me.jbusdriver.ui.adapter

import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.base.KLog
import me.jbusdriver.mvp.bean.Genre
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.bean.convertDBItem
import me.jbusdriver.mvp.bean.des
import me.jbusdriver.mvp.model.CollectModel
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.data.AppConfiguration
import me.jbusdriver.ui.data.contextMenu.LinkMenu

/**
 * Created by Administrator on 2017/7/30.
 */
open class GenreAdapter : BaseAppAdapter<Genre, BaseViewHolder>(R.layout.layout_genre_item) {


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

        setOnItemLongClickListener { adapter, view, position ->
            (adapter.data.getOrNull(position) as? Genre)?.let { item ->
                val action = (if (CollectModel.has((item as ILink).convertDBItem())) LinkMenu.linkActions.minus("收藏")
                else LinkMenu.linkActions.minus("取消收藏")).toMutableMap()

                if (AppConfiguration.enableCategory) {
                    val ac = action.remove("收藏")
                    if (ac != null) {
                        action["收藏到分类..."] = ac
                    }
                }

                MaterialDialog.Builder(view.context).title(item.name).content(item.des)
                        .items(action.keys)
                        .itemsCallback { _, _, _, text ->
                            action[text]?.invoke(item)
                        }.show()
            }
            true
        }
    }
}