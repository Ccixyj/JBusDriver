package me.jbusdriver.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.activity_watch_large_image.*
import me.jbusdriver.common.BaseActivity
import me.jbusdriver.common.KLog
import me.jbusdriver.common.inflate
import java.lang.Exception


class WatchLargeImageActivity : BaseActivity() {

    private val urls by lazy { intent.getStringArrayListExtra(INTENT_IMAGE_URL) ?: emptyList<String>() }
    private val imageViewList: ArrayList<View> = arrayListOf()
    private val index by lazy { intent.getIntExtra(INDEX, -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_large_image)
        initWidget()
    }


    private fun initWidget() {
        urls.mapTo(imageViewList) {
            this@WatchLargeImageActivity.inflate(R.layout.layout_large_image_item)
        }
        vp_largeImage.adapter = MyViewPagerAdapter()
        vp_largeImage.currentItem = if (index == -1) 0 else index
    }


    companion object {

        private const val INTENT_IMAGE_URL = "INTENT_IMAGE_URL"
        private const val INDEX = "currentIndex"
        @JvmStatic
        fun startShow(context: Context, urls: List<String>, index: Int = -1) {
            val intent = Intent()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putStringArrayListExtra(INTENT_IMAGE_URL, ArrayList(urls))
            intent.setClass(context, WatchLargeImageActivity::class.java)
            intent.putExtra(INDEX, index)
            context.startActivity(intent)
        }

    }

    inner class MyViewPagerAdapter//构造方法，参数是我们的页卡，这样比较方便。
        : PagerAdapter() {

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(imageViewList[position])//删除页卡
        }


        override fun instantiateItem(container: ViewGroup, position: Int): Any? {  //这个方法用来实例化页卡
            return imageViewList.getOrNull(position)?.apply {
                val offset = Math.abs(vp_largeImage.currentItem - position)
                val priority = when (offset) {
                    in 0..1 -> Priority.IMMEDIATE
                    in 2..5 -> Priority.HIGH
                    in 6..10 -> Priority.NORMAL
                    else -> Priority.LOW
                }
                KLog.d("load $position for ${vp_largeImage.currentItem} offset = $offset : $priority")
                container.addView(this, 0)//添加页卡
                Glide.with(this@WatchLargeImageActivity).load(urls[position])
                        .dontAnimate()
                        .error(R.drawable.ic_place_holder)
                        .priority(priority)
                        .into(object : GlideDrawableImageViewTarget(imageViewList[position].findViewById(R.id.iv_image_large) as ImageView) {
                            override fun onLoadStarted(placeholder: Drawable?) {
                                super.onLoadStarted(placeholder)
                                this@apply.findViewById(R.id.pb_large_progress).alpha = 1f

                            }

                            override fun onResourceReady(resource: GlideDrawable?, animation: GlideAnimation<in GlideDrawable>?) {
                                super.onResourceReady(resource, animation)
                                view.alpha = 0f
                                view.animate().alpha(1f).setDuration(800).start()
                                this@apply.findViewById(R.id.pb_large_progress).animate().alpha(0f).setDuration(500).start()
                            }

                            override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
                                super.onLoadFailed(e, errorDrawable)
                                this@apply.findViewById(R.id.pb_large_progress).animate().alpha(0f).setDuration(500).start()
                            }

                        })
            }
        }

        override fun getCount(): Int {
            return imageViewList.size//返回页卡的数量
        }

        override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
            return arg0 === arg1//官方提示这样写
        }
    }

}
