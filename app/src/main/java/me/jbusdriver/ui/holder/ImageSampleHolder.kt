package me.jbusdriver.ui.holder

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_detail_image_samples.view.*
import me.jbusdriver.common.GlideApp
import me.jbusdriver.common.dpToPx
import me.jbusdriver.common.inflate
import me.jbusdriver.common.toGlideUrl
import me.jbusdriver.mvp.bean.ImageSample
import me.jbusdriver.ui.activity.WatchLargeImageActivity
import me.jbusdriver.ui.adapter.BaseAppAdapter
import me.jbusdriver.ui.adapter.GridSpacingItemDecoration

/**
 * Created by Administrator on 2017/5/9 0009.
 */
class ImageSampleHolder(context: Context): BaseHolder(context) {
    val view by lazy {
        weakRef.get()?.let {
            it.inflate(R.layout.layout_detail_image_samples).apply {
                val displayMetrics = DisplayMetrics()
                (this.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displayMetrics)
                val spannCount = when (displayMetrics.widthPixels) {
                    in 0..1079 -> 3
                    in 1080..1920 -> 4
                    else -> 6
                }
                rv_recycle_images.layoutManager = GridLayoutManager(it, spannCount)
                rv_recycle_images.addItemDecoration(GridSpacingItemDecoration(spannCount, it.dpToPx(6f), false))
                imageSampleAdapter.bindToRecyclerView(rv_recycle_images)
                rv_recycle_images.isNestedScrollingEnabled = true
                imageSampleAdapter.setOnItemClickListener { _, v, position ->
                    if (position < imageSampleAdapter.data.size) {
                        WatchLargeImageActivity.startShow(v.context, imageSampleAdapter.data.map { if (TextUtils.isEmpty(it.image)) it.thumb  else it.image }, position)
                    }
                }
            }
        } ?: error("context ref is finish")
    }


    private val imageSampleAdapter = object : BaseAppAdapter<ImageSample, BaseViewHolder>(R.layout.layout_image_sample_item) {
        override fun convert(holder: BaseViewHolder, item: ImageSample) {
            weakRef.get()?.apply {
                holder.getView<ImageView>(R.id.iv_movie_thumb)?.let {
                    GlideApp.with(this).load(item.thumb.toGlideUrl)
                            .fitCenter()
                            .placeholder(R.drawable.ic_child_care_black_24dp)
                            .error(R.drawable.ic_child_care_black_24dp)
                            .into(DrawableImageViewTarget(it))

                }
            }
        }
    }

    fun init(data: List<ImageSample>) {
        //imageSamples
        if (data.isEmpty()) view.tv_movie_images_none_tip.visibility = View.VISIBLE
        else {
            //load header
            imageSampleAdapter.setNewData(data)
        }
    }

}