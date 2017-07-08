package me.jbusdriver.ui.holder

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.support.v7.graphics.Palette
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_detail_actress.view.*
import me.jbusdriver.common.KLog
import me.jbusdriver.common.SchedulersCompat
import me.jbusdriver.common.SimpleSubscriber
import me.jbusdriver.common.inflate
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.data.DataSourceType
import java.util.*

/**
 * Created by Administrator on 2017/5/9 0009.
 */
class ActressListHolder(context: Context, type: DataSourceType) : BaseHolder(context) {

    val view by lazy {
        weakRef.get()?.let {
            it.inflate(R.layout.layout_detail_actress).apply {
                rv_recycle_actress.layoutManager = LinearLayoutManager(it, LinearLayoutManager.HORIZONTAL, false)
                rv_recycle_actress.adapter = actressAdapter
                actressAdapter.setOnItemClickListener { adapter, _, position ->
                    actressAdapter.data.getOrNull(position)?.let {
                        item ->
                        KLog.d("item : $it")
                        weakRef.get()?.let {
                            MovieListActivity.start(it, type, item)
                        }
                    }

                }
            }
        } ?: error("context ref is finish")
    }

    val actressAdapter: BaseQuickAdapter<ActressInfo, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<ActressInfo, BaseViewHolder>(R.layout.layout_actress_item) {
            override fun convert(helper: BaseViewHolder, item: ActressInfo) {
                KLog.d("ActressInfo :$item")
                    Glide.with(helper.itemView.context).load(item.avatar).asBitmap().into(object : BitmapImageViewTarget(helper.getView(R.id.iv_actress_avatar)) {
                        override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                           resource?.let {
                               Flowable.just(it).map {
                                   Palette.from(it).generate()
                               }.compose(SchedulersCompat.io())
                                       .subscribeWith( object :SimpleSubscriber<Palette>(){
                                           override fun onNext(it: Palette) {
                                               super.onNext(it)
                                               val swatch = listOf(it.lightMutedSwatch, it.lightVibrantSwatch, it.vibrantSwatch, it.mutedSwatch).filterNotNull()
                                               if (!swatch.isEmpty()) {
                                                   swatch[randomNum(swatch.size)].let {
                                                       helper.setBackgroundColor(R.id.tv_actress_name, it.rgb)
                                                       helper.setTextColor(R.id.tv_actress_name, it.bodyTextColor)
                                                   }
                                               }
                                           }
                                       })
                                       .addTo(rxManager)

                             /*   Palette.from(it).generate {
                                    val swatch = listOf(it.lightMutedSwatch, it.lightVibrantSwatch, it.vibrantSwatch, it.mutedSwatch).filterNotNull()
                                    if (!swatch.isEmpty()) {
                                        swatch[randomNum(swatch.size)].let {
                                            helper.setBackgroundColor(R.id.tv_actress_name, it.rgb)
                                            helper.setTextColor(R.id.tv_actress_name, it.bodyTextColor)
                                        }
                                    }
                                }.let {
                                    paletteReq.add(it)
                                }*/
                            }
                            super.onResourceReady(resource, glideAnimation)
                        }
                    })
                //加载名字
                helper.setText(R.id.tv_actress_name, item.name)
            }
        }
    }


    val random = Random()
    private fun randomNum(number: Int): Int {
        return Math.abs(random.nextInt() % number)
    }

    fun init(actress: List<ActressInfo>) {
        //actress
        if (actress.isEmpty()) view.tv_movie_actress_none_tip.visibility = View.VISIBLE
        else {
            //load header
            actressAdapter.setNewData(actress)
        }
    }

}