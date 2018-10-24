package me.jbusdriver.ui.adapter

import android.graphics.Bitmap
import android.support.v7.graphics.Palette
import android.text.TextUtils
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.chad.library.adapter.base.BaseViewHolder
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.base.GlideApp
import me.jbusdriver.base.KLog
import me.jbusdriver.base.SchedulersCompat
import me.jbusdriver.base.SimpleSubscriber
import me.jbusdriver.base.glide.toGlideNoHostUrl
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.convertDBItem
import me.jbusdriver.mvp.model.CollectModel
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.data.AppConfiguration
import me.jbusdriver.ui.data.contextMenu.LinkMenu
import java.util.*

class ActressInfoAdapter(val rxManager: CompositeDisposable) : BaseAppAdapter<ActressInfo, BaseViewHolder>(R.layout.layout_actress_item) {


    private val random = Random()
    private fun randomNum(number: Int) = Math.abs(random.nextInt() % number)

    override fun convert(holder: BaseViewHolder, item: ActressInfo) {
        KLog.d("ActressInfo :$item")
        GlideApp.with(holder.itemView.context).asBitmap().load(item.avatar.toGlideNoHostUrl)
                .error(R.drawable.ic_nowprinting).into(object : BitmapImageViewTarget(holder.getView(R.id.iv_actress_avatar)) {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        Flowable.just(resource).map {
                            Palette.from(resource).generate()
                        }.compose(SchedulersCompat.io())
                                .subscribeWith(object : SimpleSubscriber<Palette>() {
                                    override fun onNext(it: Palette) {
                                        super.onNext(it)
                                        val swatch = listOfNotNull(it.lightMutedSwatch, it.lightVibrantSwatch, it.vibrantSwatch, it.mutedSwatch)
                                        if (!swatch.isEmpty()) {
                                            swatch[randomNum(swatch.size)].let {
                                                holder.setBackgroundColor(R.id.tv_actress_name, it.rgb)
                                                holder.setTextColor(R.id.tv_actress_name, it.bodyTextColor)
                                            }
                                        }
                                    }
                                })
                                .addTo(rxManager)

                        super.onResourceReady(resource, transition)
                    }
                })
        //加载名字
        holder.setText(R.id.tv_actress_name, item.name)

        holder.setText(R.id.tv_actress_tag, item.tag)
        holder.setVisible(R.id.tv_actress_tag, !TextUtils.isEmpty(item.tag))
    }

    init {

        setOnItemClickListener { _, view, position ->
            data.getOrNull(position)?.let { item ->
                KLog.d("item : $item")
                MovieListActivity.start(view.context, item)
            }
        }

        setOnItemLongClickListener { _, view, position ->
            data.getOrNull(position)?.let { act ->
                val action = (if (CollectModel.has(act.convertDBItem())) LinkMenu.actressActions.minus("收藏")
                else LinkMenu.actressActions.minus("取消收藏")).toMutableMap()

                if (AppConfiguration.enableCategory) {
                    val ac = action.remove("收藏")
                    if (ac != null) {
                        action["收藏到分类..."] = ac
                    }
                }
                MaterialDialog.Builder(view.context).title(act.name)
                        .items(action.keys)
                        .itemsCallback { _, _, _, text ->
                            action[text]?.invoke(act)
                        }
                        .show()
            }
            true
        }
    }


}