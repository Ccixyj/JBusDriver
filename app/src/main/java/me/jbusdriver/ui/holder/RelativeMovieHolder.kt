package me.jbusdriver.ui.holder

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.graphics.Palette
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_detail_relative_movies.view.*
import me.jbusdriver.common.KLog
import me.jbusdriver.common.inflate
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.ui.activity.MovieDetailActivity
import java.util.*

/**
 * Created by Administrator on 2017/5/9 0009.
 */
class RelativeMovieHolder(context: Context) {

    val view by lazy {
        context.inflate(R.layout.layout_detail_relative_movies).apply {
            rv_recycle_relative_movies.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            rv_recycle_relative_movies.adapter = relativeAdapter
            relativeAdapter.setOnItemClickListener { adapter, _, position ->
                relativeAdapter.data.getOrNull(position)?.let {
                    KLog.d("relative  : $it")
                    MovieDetailActivity.start(context, it)
                }

            }
        }
    }

    val relativeAdapter = object : BaseQuickAdapter<Movie, BaseViewHolder>(R.layout.layout_detail_relative_movies_item) {
        override fun convert(helper: BaseViewHolder, item: Movie) {
            Glide.with(context).load(item.imageUrl).asBitmap().into(object : BitmapImageViewTarget(helper.getView(R.id.iv_relative_movie_image)) {
                override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                    resource?.let {
                        Palette.from(it).generate {
                            val swatch = listOf(it.lightMutedSwatch, it.lightVibrantSwatch, it.vibrantSwatch, it.mutedSwatch).filterNotNull()
                            if (!swatch.isEmpty()) {
                                swatch[randomNum(swatch.size)].let {
                                    helper.setBackgroundColor(R.id.tv_relative_movie_title, it.rgb)
                                    helper.setTextColor(R.id.tv_relative_movie_title, it.bodyTextColor)
                                }
                            }
                        }
                    }
                    super.onResourceReady(resource, glideAnimation)
                }
            })
            //加载名字
            helper.setText(R.id.tv_relative_movie_title, item.title)
        }
    }


    val random = Random()
    private fun randomNum(number: Int): Int {
        return Math.abs(random.nextInt() % number)
    }

    fun init(relativeMovies: List<Movie>) {
        //actress
        KLog.d("relate moview : $relativeMovies")
        if (relativeMovies.isEmpty()) view.tv_movie_relative_none_tip.visibility = View.VISIBLE
        else {
            //load header
            relativeAdapter.setNewData(relativeMovies)
        }
    }
}