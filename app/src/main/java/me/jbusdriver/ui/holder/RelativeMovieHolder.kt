package me.jbusdriver.ui.holder

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.graphics.Palette
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_detail_relative_movies.view.*
import me.jbusdriver.common.*
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.ui.activity.MovieDetailActivity
import me.jbusdriver.ui.data.CollectManager
import java.util.*

/**
 * Created by Administrator on 2017/5/9 0009.
 */
class RelativeMovieHolder(context: Context) : BaseHolder(context) {

    val actionMap by lazy {
        mapOf("复制名字" to { movie: Movie ->
            weakRef.get()?.let {
                it.copy(movie.title)
                it.toast("已复制")

            }
        }, "收藏" to { movie: Movie ->
            CollectManager.addToCollect(movie)
            KLog.d("movie_data:${CollectManager.movie_data}")
        }, "取消收藏" to { movie: Movie ->
            CollectManager.removeCollect(movie)
            KLog.d("movie_data:${CollectManager.movie_data}")
        })
    }

    val view by lazy {
        weakRef.get()?.let {
            it.inflate(R.layout.layout_detail_relative_movies).apply {
                rv_recycle_relative_movies.layoutManager = LinearLayoutManager(it, LinearLayoutManager.HORIZONTAL, false)
                rv_recycle_relative_movies.adapter = relativeAdapter
                relativeAdapter.setOnItemClickListener { adapter, v, position ->
                    relativeAdapter.data.getOrNull(position)?.let {
                        KLog.d("relative  : $it")
                        MovieDetailActivity.start(v.context, it)
                    }
                }
                relativeAdapter.setOnItemLongClickListener { adapter, view, position ->
                    relativeAdapter.data.getOrNull(position)?.let {
                        movie ->
                        val action = if (CollectManager.has(movie)) actionMap.minus("收藏")
                        else actionMap.minus("取消收藏")
                        MaterialDialog.Builder(view.context).title(movie.title)
                                .items(action.keys)
                                .itemsCallback { _, _, _, text ->
                                    actionMap[text]?.invoke(movie)
                                }
                                .show()
                    }
                    return@setOnItemLongClickListener true
                }
            }
        } ?: error("context ref is finish")
    }

    val relativeAdapter: BaseQuickAdapter<Movie, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<Movie, BaseViewHolder>(R.layout.layout_detail_relative_movies_item) {
            override fun convert(holder: BaseViewHolder, item: Movie) {
                Glide.with(holder.itemView.context).load(item.imageUrl.toGlideUrl).asBitmap().into(object : BitmapImageViewTarget(holder.getView(R.id.iv_relative_movie_image)) {
                    override fun setResource(resource: Bitmap?) {
                        super.setResource(resource)
                        resource?.let {

                            Flowable.just(it).map {
                                Palette.from(it).generate()
                            }.compose(SchedulersCompat.io())
                                    .subscribeWith(object : SimpleSubscriber<Palette>() {
                                        override fun onNext(it: Palette) {
                                            super.onNext(it)
                                            val swatch = listOf(it.lightMutedSwatch, it.lightVibrantSwatch, it.vibrantSwatch, it.mutedSwatch).filterNotNull()
                                            if (!swatch.isEmpty()) {
                                                swatch[randomNum(swatch.size)].let {
                                                    holder.setBackgroundColor(R.id.tv_relative_movie_title, it.rgb)
                                                    holder.setTextColor(R.id.tv_relative_movie_title, it.bodyTextColor)
                                                }
                                            }
                                        }
                                    })
                                    .addTo(rxManager)


                            /*  Palette.from(it).generate {

                              }.let {
                                  paletteReq.add(it)
                              }*/
                        }
                    }
                })
                //加载名字
                holder.setText(R.id.tv_relative_movie_title, item.title)
            }
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