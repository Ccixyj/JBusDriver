package me.jbusdriver.ui.holder

import android.content.Context
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_detail_image_samples.view.*
import me.jbusdriver.common.dpToPx
import me.jbusdriver.common.inflate
import me.jbusdriver.mvp.bean.ImageSample
import me.jbusdriver.ui.activity.WatchLargeImageActivity
import me.jbusdriver.ui.data.DataSourceType
import me.jbusdriver.ui.data.GridSpacingItemDecoration

/**
 * Created by Administrator on 2017/5/9 0009.
 */
class ImageSampleHolder(context: Context, type: DataSourceType) {
    val view by lazy {
        context.inflate(R.layout.layout_detail_image_samples, null).apply {
            val displayMetrics = DisplayMetrics()
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displayMetrics)
            val spannCount = when (displayMetrics.widthPixels) {
                in 0..1079 -> 3
                in 1080..1920 -> 4
                else -> 6
            }
            rv_recycle_images.layoutManager = StaggeredGridLayoutManager(spannCount, StaggeredGridLayoutManager.VERTICAL)
            rv_recycle_images.addItemDecoration(GridSpacingItemDecoration(spannCount, context.dpToPx(6f), false))
            rv_recycle_images.adapter = imageSampleAdapter
            imageSampleAdapter.setOnItemClickListener { _, _, position ->
                if (position < imageSampleAdapter.data.size) {
                    WatchLargeImageActivity.startShow(context, imageSampleAdapter.data.map { it.image }, position)
                }
            }
        }
    }


    val imageSampleAdapter = object : BaseQuickAdapter<ImageSample, BaseViewHolder>(R.layout.layout_image_sample_item) {

        override fun convert(helper: BaseViewHolder, item: ImageSample) {
            helper.getView<ImageView>(R.id.iv_movie_thumb)?.let {
                Glide.with(context).load(item.thumb)
                        .fitCenter()
                        .placeholder(R.drawable.ic_child_care_black_24dp)
                        .error(R.drawable.ic_child_care_black_24dp)
                        .into(GlideDrawableImageViewTarget(it))

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