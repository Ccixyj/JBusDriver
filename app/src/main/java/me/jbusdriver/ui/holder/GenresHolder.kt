package me.jbusdriver.ui.holder

import android.content.Context
import android.view.View
import com.xiaofeng.flowlayoutmanager.FlowLayoutManager
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_detail_genres.view.*
import me.jbusdriver.base.inflate
import me.jbusdriver.mvp.bean.Genre
import me.jbusdriver.ui.adapter.GenreAdapter

/**
 * Created by Administrator on 2017/5/10 0010.
 */
class GenresHolder(context: Context) : BaseHolder(context) {
    val view by lazy {
        weakRef.get()?.let {
            it.inflate(R.layout.layout_detail_genres).apply {
                rv_recycle_genres.layoutManager = FlowLayoutManager().apply { isAutoMeasureEnabled = true }
                genreAdapter.bindToRecyclerView(rv_recycle_genres)
                rv_recycle_genres.isNestedScrollingEnabled = true
            }
        } ?: error("context ref is finish")
    }

    private val genreAdapter = GenreAdapter()

    fun init(genres: List<Genre>) {
        //actress
        if (genres.isEmpty()) view.tv_movie_genres_none_tip.visibility = View.VISIBLE
        else {
            //load header
            genreAdapter.setNewData(genres)
        }
    }
}