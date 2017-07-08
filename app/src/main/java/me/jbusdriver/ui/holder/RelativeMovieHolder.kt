package me.jbusdriver.ui.holder

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.support.v7.graphics.Palette
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_detail_relative_movies.view.*
import me.jbusdriver.common.KLog
import me.jbusdriver.common.SchedulersCompat
import me.jbusdriver.common.SimpleSubscriber
import me.jbusdriver.common.inflate
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.ui.activity.MovieDetailActivity
import java.util.*

/**
 * Created by Administrator on 2017/5/9 0009.
 */
class RelativeMovieHolder(context: Context) : BaseHolder(context){
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
            }
        }?: error("context ref is finish")
    }

    val relativeAdapter :BaseQuickAdapter<Movie, BaseViewHolder> by  lazy {
        object : BaseQuickAdapter<Movie, BaseViewHolder>(R.layout.layout_detail_relative_movies_item) {
            override fun convert(helper: BaseViewHolder, item: Movie) {
                Glide.with(helper.itemView.context).load(item.imageUrl).asBitmap().into(object : BitmapImageViewTarget(helper.getView(R.id.iv_relative_movie_image)) {
                    override fun setResource(resource: Bitmap?) {
                        super.setResource(resource)
                        resource?.let {

                            Flowable.just(it).map {
                                Palette.from(it).generate()
                            }.compose(SchedulersCompat.io())
                                    .subscribeWith( object : SimpleSubscriber<Palette>(){
                                        override fun onNext(it: Palette) {
                                            super.onNext(it)
                                            val swatch = listOf(it.lightMutedSwatch, it.lightVibrantSwatch, it.vibrantSwatch, it.mutedSwatch).filterNotNull()
                                            if (!swatch.isEmpty()) {
                                                swatch[randomNum(swatch.size)].let {
                                                    helper.setBackgroundColor(R.id.tv_relative_movie_title, it.rgb)
                                                    helper.setTextColor(R.id.tv_relative_movie_title, it.bodyTextColor)
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
                helper.setText(R.id.tv_relative_movie_title, item.title)
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