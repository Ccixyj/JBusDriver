package me.jbusdriver.ui.adapter

import android.graphics.Bitmap
import android.support.v7.graphics.Palette
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.chad.library.adapter.base.BaseViewHolder
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.common.*
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.data.collect.ActressCollector
import java.util.*

class ActressInfoAdapter(val rxManager: CompositeDisposable) : BaseAppAdapter<ActressInfo, BaseViewHolder>(R.layout.layout_actress_item) {
    private val actionMap by lazy {
        mapOf("复制名字" to { act: ActressInfo ->
            AppContext.instace.copy(act.name)
            AppContext.instace.toast("已复制")
        }, "收藏" to { act: ActressInfo ->
            ActressCollector.addToCollect(act)
            KLog.d("actress_data:${ActressCollector.dataList}")
        }, "取消收藏" to { act: ActressInfo ->
            ActressCollector.removeCollect(act)
            KLog.d("actress_data:${ActressCollector.dataList}")
        })
    }


    private val random = Random()
    private fun randomNum(number: Int) = Math.abs(random.nextInt() % number)

    override fun convert(holder: BaseViewHolder, item: ActressInfo) {
        KLog.d("ActressInfo :$item")
        GlideApp.with(holder.itemView.context).asBitmap().load(item.avatar.toGlideUrl)
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
                val action = if (ActressCollector.has(act)) actionMap.minus("收藏")
                else actionMap.minus("取消收藏")

                MaterialDialog.Builder(view.context).title(act.name)
                        .items(action.keys)
                        .itemsCallback { _, _, _, text ->
                            actionMap[text]?.invoke(act)
                        }
                        .show()
            }
            true
        }
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
    }
}