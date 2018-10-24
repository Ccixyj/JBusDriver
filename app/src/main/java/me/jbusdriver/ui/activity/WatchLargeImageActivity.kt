package me.jbusdriver.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.gyf.barlibrary.ImmersionBar
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.activity_watch_large_image.*
import kotlinx.android.synthetic.main.layout_large_image_item.view.*
import me.jbusdriver.base.*
import me.jbusdriver.base.common.BaseActivity
import me.jbusdriver.base.http.OnProgressListener
import me.jbusdriver.base.http.addProgressListener
import me.jbusdriver.base.http.removeProgressListener
import me.jbusdriver.common.JBus
import me.jbusdriver.base.glide.toGlideNoHostUrl
import java.io.File
import java.util.concurrent.TimeUnit


class WatchLargeImageActivity : BaseActivity() {

    private val urls by lazy {
        intent.getStringArrayListExtra(INTENT_IMAGE_URL) ?: emptyList<String>()
    }
    private val imageViewList: ArrayList<View> = arrayListOf()
    private val index by lazy { intent.getIntExtra(INDEX, -1) }
    private val imageSaveDir by lazy {
        val pathSuffix = File.separator + "download" + File.separator + "image" + File.separator
        createDir(Environment.getExternalStorageDirectory().absolutePath + File.separator + JBus.packageName + pathSuffix)
                ?: createDir(JBus.externalCacheDir.absolutePath + JBus.packageName + pathSuffix)
                ?: error("cant not create collect dir in anywhere")

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_large_image)
        initWidget()
    }


    @SuppressLint("SetTextI18n")
    private fun initWidget() {
        immersionBar.transparentBar().init()
        val statusBarHeight = ImmersionBar.getStatusBarHeight(this)

        urls.mapTo(imageViewList) {
            this@WatchLargeImageActivity.inflate(R.layout.layout_large_image_item).apply {
                (pb_hor_progress.layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin = statusBarHeight
            }
        }

        vp_largeImage.adapter = MyViewPagerAdapter()
        vp_largeImage.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                this@WatchLargeImageActivity.tv_url_index.text = "${position + 1} / ${imageViewList.size}"
            }
        })
        vp_largeImage.currentItem = if (index == -1) 0 else index
        this@WatchLargeImageActivity.tv_url_index.text = "${vp_largeImage.currentItem + 1} / ${imageViewList.size}"

        iv_download.setOnClickListener {
            KLog.d("download image ${urls[vp_largeImage.currentItem]}")
            Schedulers.io().scheduleDirect {
                val url = urls[vp_largeImage.currentItem]
                GlideApp.with(this).asFile().load(url).submit()
                        .get(3, TimeUnit.SECONDS)?.let {
                            //copy file
                            val fileName = url.urlPath.split("/").lastOrNull() ?: kotlin.run {
                                viewContext.toast("无法获取文件名！")
                                return@scheduleDirect
                            }
                            it.copyTo(File(imageSaveDir + fileName), true)
                            viewContext.toast("文件保存至${imageSaveDir}下")
                        }
            }.addTo(rxManager)

        }

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
                    .load(url.toGlideNoHostUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.ic_image_error)
                    .fitCenter()
                    .priority(priority)
                    .into(object : DrawableImageViewTarget(view.pv_image_large) {
                        val listener = object : OnProgressListener {
                            override fun onProgress(imageUrl: String, bytesRead: Long, totalBytes: Long, isDone: Boolean, exception: GlideException?) {
                                if (totalBytes == 0L) return
                                if (url != imageUrl) return
                                postMain {
                                    //view.pb_hor_progress.visibility = View.GONE
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

                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            super.onResourceReady(resource, transition)
                            view.pb_hor_progress?.animate()?.alpha(0f)?.setDuration(300)?.start()
                            removeProgressListener(listener)
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            removeProgressListener(listener)
                            view.pb_hor_progress?.animate()?.alpha(0f)?.setDuration(300)?.start()
                        }

                    })
        }

        override fun getCount() = imageViewList.size//返回页卡的数量


        override fun isViewFromObject(arg0: View, arg1: Any) = arg0 === arg1//官方提示这样写
    }


}
