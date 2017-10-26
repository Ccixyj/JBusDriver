package me.jbusdriver.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.jaeger.library.StatusBarUtil
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.activity_watch_large_image.*
import me.jbusdriver.common.*
import me.jbusdriver.ui.widget.ImageGestureListener
import me.jbusdriver.ui.widget.MultiTouchZoomableImageView
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
        StatusBarUtil.setTransparent(this)
    }


    companion object {

        private const val INTENT_IMAGE_URL = "INTENT_IMAGE_URL"
        private const val INDEX = "currentIndex"
        @JvmStatic
        fun startShow(context: Context, urls: List<String>, index: Int = -1) {
            val intent = Intent(context, WatchLargeImageActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putStringArrayListExtra(INTENT_IMAGE_URL, ArrayList(urls))
            intent.putExtra(INDEX, index)
            context.startActivity(intent)
        }

    }

    inner class MyViewPagerAdapter : PagerAdapter() {

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(imageViewList[position])//删除页卡
        }


        override fun instantiateItem(container: ViewGroup, position: Int): Any? {  //这个方法用来实例化页卡
            return imageViewList.getOrNull(position)?.apply {
                container.addView(this, 0)//添加页卡
                loadImage(this, position)
            }
        }

        private fun loadImage(view: View, position: Int) {
            view.findViewById(R.id.pb_large_progress).animate().alpha(1f).setDuration(300).start()
            val offset = Math.abs(vp_largeImage.currentItem - position)
            val priority = when (offset) {
                in 0..1 -> Priority.IMMEDIATE
                in 2..5 -> Priority.HIGH
                in 6..10 -> Priority.NORMAL
                else -> Priority.LOW
            }
            KLog.d("load $position for ${vp_largeImage.currentItem} offset = $offset : $priority")

            GlideApp.with(this@WatchLargeImageActivity).load((urls[position]).toGlideUrl)
                    .crossFade()
                    .error(R.drawable.ic_image_broken)
                    .priority(priority)
                    .into(object : SimpleTarget<Bitmap>() {

                        override fun onLoadStarted(placeholder: Drawable?) {
                            super.onLoadStarted(placeholder)
                            view.findViewById(R.id.pb_large_progress).alpha = 1f
                        }

                        override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                            (view.findViewById(R.id.mziv_image_large) as MultiTouchZoomableImageView).imageBitmap = resource
                            view.findViewById(R.id.pb_large_progress).animate().alpha(0f).setDuration(300).start()
                        }

                        override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
                            super.onLoadFailed(e, errorDrawable)
                            view.findViewById(R.id.pb_large_progress).animate().alpha(0f).setDuration(500).start()
                            (view.findViewById(R.id.mziv_image_large) as MultiTouchZoomableImageView).apply {
                                imageBitmap = BitmapFactory.decodeResource(viewContext.resources, R.drawable.ic_image_broken)
                                setImageGestureListener(object : ImageGestureListener {
                                    override fun onImageGestureSingleTapConfirmed() {
                                        KLog.e("onImageGestureSingleTapConfirmed : reload")
                                        imageBitmap.recycle()
                                        loadImage(view, position)
                                    }

                                    override fun onImageGestureLongPress() {
                                    }

                                    override fun onImageGestureFlingDown() {
                                    }
                                })
                            }
                        }

                    })
        }

        override fun getCount(): Int {
            return imageViewList.size//返回页卡的数量
        }



        override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
            return arg0 === arg1//官方提示这样写
        }
    }

}
