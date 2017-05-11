package me.jbusdriver.ui.holder

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.xiaofeng.flowlayoutmanager.FlowLayoutManager
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_detail_genres.view.*
import me.jbusdriver.common.KLog
import me.jbusdriver.common.inflate
import me.jbusdriver.mvp.bean.Genre
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.data.DataSourceType

/**
 * Created by Administrator on 2017/5/10 0010.
 */
class GenresHolder(context: Context, type: DataSourceType) {
    val view by lazy {
        context.inflate(R.layout.layout_detail_genres).apply {
            rv_recycle_genres.layoutManager = FlowLayoutManager().apply { isAutoMeasureEnabled = true }
            rv_recycle_genres.adapter = genreAdapter
            genreAdapter.setOnItemClickListener { _, _, position ->
                genreAdapter.data.getOrNull(position)?.let {
                    KLog.d("genre : $it")
                    MovieListActivity.start(context, type, it)

                }
            }
        }
    }

    val genreAdapter = object : BaseQuickAdapter<Genre, BaseViewHolder>(R.layout.layout_genre_item) {
        override fun convert(helper: BaseViewHolder, item: Genre) {
            helper.setText(R.id.tv_movie_genre, item.name)
            (helper.getView<TextView>(R.id.tv_movie_genre).background as? GradientDrawable)?.apply {
                setColor(context.resources.getColor(R.color.colorPrimary))
            }
        }
    }

    fun init(genres: List<Genre>) {
        //actress
        if (genres.isEmpty()) view.tv_movie_genres_none_tip.visibility = View.VISIBLE
        else {
            //load header
            genreAdapter.setNewData(genres)
        }
    }
}