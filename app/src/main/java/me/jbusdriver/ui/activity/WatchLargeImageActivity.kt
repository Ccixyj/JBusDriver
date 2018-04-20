package me.jbusdriver.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.gyf.barlibrary.ImmersionBar
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.activity_watch_large_image.*
import kotlinx.android.synthetic.main.layout_large_image_item.view.*
import me.jbusdriver.common.*
import me.jbusdriver.ui.widget.ImageGestureListener


class WatchLargeImageActivity : BaseActivity() {

    private val urls by lazy {
        intent.getStringArrayListExtra(INTENT_IMAGE_URL) ?: emptyList<String>()
    }
    private val imageViewList: ArrayList<View> = arrayListOf()
    private val index by lazy { intent.getIntExtra(INDEX, -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_large_image)
        initWidget()
    }


    private fun initWidget() {
        val statusBarHeight = ImmersionBar.getStatusBarHeight(this)
        urls.mapTo(imageViewList) {
            this@WatchLargeImageActivity.inflate(R.layout.layout_large_image_item).apply {
                this.mziv_image_large.setViewPager(vp_largeImage)
                (pb_hor_progress.layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin = statusBarHeight
            }
        }
        vp_largeImage.adapter = MyViewPagerAdapter()
        vp_largeImage.currentItem = if (index == -1) 0 else index

        immersionBar.transparentBar().init()

    }


    companion object {

        private const val INTENT_IMAGE_URL = "INTENT_IMAGE_URL"
        private const val INDEX = "currentIndex"

        fun startShow(context: Context, urls: List<String>, index: Int = -1) {
            val intent = Intent(context, WatchLargeImageActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putStringArrayListExtra(INTENT_IMAGE_URL, ArrayList(urls))
            intent.putExtra(INDEX, index)
            context.startActivity(intent)
        }

    }

    inner class MyViewPagerAdapter : PagerAdapter() {
        private val handler by lazy { Handler(Looper.getMainLooper()) }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(imageViewList[position])//删除页卡
        }


        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            return imageViewList.getOrNull(position)?.apply {
                container.addView(this, 0)//添加页卡
                loadImage(this, position)
            } ?: error("can not instantiateItem for $position in $imageViewList")
        }

        private fun loadImage(view: View, position: Int) {
            view.findViewById<View>(R.id.pb_hor_progress)?.animate()?.alpha(1f)?.setDuration(300)?.start()
            val offset = Math.abs(vp_largeImage.currentItem - position)
            val priority = when (offset) {
                in 0..1 -> Priority.IMMEDIATE
                in 2..5 -> Priority.HIGH
                in 6..10 -> Priority.NORMAL
                else -> Priority.LOW
            }
            KLog.d("load $position for ${vp_largeImage.currentItem} offset = $offset : $priority")
            val url = urls[position]
            GlideApp.with(this@WatchLargeImageActivity)
                    .asBitmap()
                    .load(url.toGlideUrl)
                    .transition(withCrossFade())
                    .error(R.drawable.ic_image_error)
                    .priority(priority)
                    .into(object : SimpleTarget<Bitmap>() {
                        val listener = object : OnProgressListener {
                            override fun onProgress(imageUrl: String, bytesRead: Long, totalBytes: Long, isDone: Boolean, exception: GlideException?) {
                                if (totalBytes == 0L) return
                                if (url != imageUrl) return
                                handler.post {
                                    //                                    view.pb_hor_progress.visibility = View.GONE
                                    view.pb_hor_progress.isIndeterminate = false
                                    view.pb_hor_progress?.apply {
                                        progress = (bytesRead * 1.0f / totalBytes * 100.0f).toInt()
                                    }
                                }

                                if (isDone) {
                                    removeProgressListener(this)
                                }
                            }
                        }

                        override fun onLoadStarted(placeholder: Drawable?) {
                            view.pb_hor_progress?.animate()?.alpha(1f)?.setDuration(300)?.start()
                            addProgressListener(listener)
                            super.onLoadStarted(placeholder)
                        }

                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            view.mziv_image_large?.imageBitmap = resource
                            view.pb_hor_progress?.animate()?.alpha(0f)?.setDuration(300)?.start()
                            removeProgressListener(listener)
                        }


                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            removeProgressListener(listener)
                            view.pb_hor_progress?.animate()?.alpha(0f)?.setDuration(300)?.start()
                            (view.mziv_image_large)?.also { iv ->
                                //                                imageBitmap = BitmapFactory.decodeResource(viewContext.resources, R.drawable.ic_image_error)
                                GlideApp.with(view).asBitmap().load(R.drawable.ic_image_error).into(object : SimpleTarget<Bitmap>() {
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                        iv.imageBitmap = resource
                                    }
                                })

                                iv.setImageGestureListener(object : ImageGestureListener {
                                    override fun onImageGestureSingleTapConfirmed() {
                                        KLog.e("onImageGestureSingleTapConfirmed : reload")
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

        override fun getCount() = imageViewList.size//返回页卡的数量


        override fun isViewFromObject(arg0: View, arg1: Any) = arg0 === arg1//官方提示这样写
    }


}
