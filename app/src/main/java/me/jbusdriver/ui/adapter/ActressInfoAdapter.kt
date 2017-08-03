package me.jbusdriver.ui.adapter

import android.graphics.Bitmap
import android.support.v7.graphics.Palette
import android.text.TextUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.common.KLog
import me.jbusdriver.common.SchedulersCompat
import me.jbusdriver.common.SimpleSubscriber
import me.jbusdriver.mvp.bean.ActressInfo
import java.util.*

/**
 * Created by Administrator on 2017/7/17.
 */
class ActressInfoAdapter(val rxManager: CompositeDisposable) : BaseQuickAdapter<ActressInfo, BaseViewHolder>(R.layout.layout_actress_item) {
    val random = Random()
    private fun randomNum(number: Int): Int {
        return Math.abs(random.nextInt() % number)
    }

    override fun convert(holder: BaseViewHolder, item: ActressInfo) {
        KLog.d("ActressInfo :$item")
        Glide.with(holder.itemView.context).load(item.avatar).asBitmap()
                .error(R.drawable.ic_nowprinting).into(object : BitmapImageViewTarget(holder.getView(R.id.iv_actress_avatar)) {
            override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
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
                                            holder.setBackgroundColor(R.id.tv_actress_name, it.rgb)
                                            holder.setTextColor(R.id.tv_actress_name, it.bodyTextColor)
                                        }
                                    }
                                }
                            })
                            .addTo(rxManager)
                }
                super.onResourceReady(resource, glideAnimation)
            }
        })
        //加载名字
        holder.setText(R.id.tv_actress_name, item.name)

        holder.setText(R.id.tv_actress_tag, item.tag)
        holder.setVisible(R.id.tv_actress_tag, !TextUtils.isEmpty(item.tag))
    }
}