package me.jbusdriver.ui.adapter

import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.common.KLog
import me.jbusdriver.mvp.bean.Genre
import me.jbusdriver.ui.activity.MovieListActivity

/**
 * Created by Administrator on 2017/7/30.
 */
open class GenreAdapter : BaseQuickAdapter<Genre, BaseViewHolder>(R.layout.layout_genre_item) {
    override fun convert(holder: BaseViewHolder, item: Genre) {
        holder.setText(R.id.tv_movie_genre, item.name)
        (holder.getView<TextView>(R.id.tv_movie_genre).background as? GradientDrawable)?.apply {
            setColor(holder.itemView.resources.getColor(R.color.colorPrimary))
        }
    }

    init {

        setOnItemClickListener { _, view, position ->
            data.getOrNull(position)?.let {
                genre ->
                KLog.d("genre : $genre")
                MovieListActivity.start(view.context, genre)
            }

        }
    }
}