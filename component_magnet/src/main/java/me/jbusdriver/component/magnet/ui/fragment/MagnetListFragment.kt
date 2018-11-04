package me.jbusdriver.component.magnet.ui.fragment

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.comp_magnet_layout_swipe_recycle.*
import me.jbusdriver.base.*
import me.jbusdriver.base.common.AppBaseRecycleFragment
import me.jbusdriver.base.common.C
import me.jbusdriver.component.magnet.R
import me.jbusdriver.component.magnet.bean.Magnet
import me.jbusdriver.component.magnet.loader.IMagnetLoader
import me.jbusdriver.component.magnet.loader.IMagnetLoader.Companion.MagnetFormatPrefix
import me.jbusdriver.component.magnet.mvp.MagnetListContract.MagnetListPresenter
import me.jbusdriver.component.magnet.mvp.MagnetListContract.MagnetListView
import me.jbusdriver.component.magnet.mvp.presenter.MagnetListPresenterImpl
import org.jsoup.Jsoup

class MagnetListFragment : AppBaseRecycleFragment<MagnetListPresenter, MagnetListView, Magnet>(), MagnetListView {

    private val keyword by lazy { arguments?.getString(C.BundleKey.Key_1) ?: error("need keyword") }
    private val magnetLoaderKey by lazy {
        arguments?.getString(C.BundleKey.Key_2) ?: error("need magnet loaderKey")
    }

    override fun createPresenter() = MagnetListPresenterImpl(magnetLoaderKey, keyword)

    override val layoutId: Int = R.layout.comp_magnet_layout_swipe_recycle
    override val swipeView: SwipeRefreshLayout?  by lazy { comp_magnet_sr_refresh }
    override val recycleView: RecyclerView by lazy { comp_magnet_rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager  by lazy { LinearLayoutManager(viewContext) }


    override val adapter: BaseQuickAdapter<Magnet, in BaseViewHolder> by lazy {

        object : BaseQuickAdapter<Magnet, BaseViewHolder>(R.layout.comp_magnet_layout_magnet_item) {

            override fun convert(helper: BaseViewHolder, item: Magnet) {
                helper.setText(R.id.comp_magnet_tv_magnet_title, item.name)
                        .setText(R.id.comp_magnet_tv_magnet_date, item.date)
                        .setText(R.id.comp_magnet_tv_magnet_size, item.size)
                        .addOnClickListener(R.id.comp_magnet_iv_magnet_copy)
            }

        }.apply {

            fun tryGetMagnet(url: String): Flowable<String> {
                return Flowable.just(url).flatMap { url ->
                    if (url.startsWith(MagnetFormatPrefix)) {
                        Flowable.just(url)
                    } else {
                        Flowable.fromCallable { Jsoup.connect(url).get().select(".content .infohash a").text().trim() }
                                .map { IMagnetLoader.MagnetFormatPrefix + it }
                                .addUserCase(sec = 6)
                                .onErrorReturn { url }
                    }
                }
            }

            setOnItemClickListener { adapter, _, position ->
                (adapter.data.getOrNull(position) as? Magnet)?.let { magnet ->
                    showMagnetLoading()
                    tryGetMagnet(magnet.link)
                            .compose(SchedulersCompat.io()).subscribeBy {
                                this@MagnetListFragment.adapter.setData(position, magnet.copy(link = it))
                                viewContext.browse(it) {
                                    placeDialogHolder?.dismiss()
                                }
                            }.addTo(rxManager)

                }

            }

            setOnItemChildClickListener { adapter, view, position ->
                (adapter.data.getOrNull(position) as? Magnet)?.let { magnet ->
                    when (view.id) {
                        R.id.comp_magnet_iv_magnet_copy -> {

                            tryGetMagnet(magnet.link).compose(SchedulersCompat.io()).subscribeBy { url->
                                this@MagnetListFragment.adapter.setData(position, magnet.copy(link = url))
                                view.context.apply {
                                    copy(url)
                                    toast("复制成功")
                                }
                            }.addTo(rxManager)


                        }
                        else -> Unit

                    }

                }
            }
        }

    }


    private fun showMagnetLoading() {
        placeDialogHolder = MaterialDialog.Builder(viewContext).content("正在查询磁力信息...").progress(true, 0).show()
    }

    override fun onPause() {
        super.onPause()
        placeDialogHolder?.dismiss()
    }

    companion object {
        fun newInstance(keyword: String, loaderKey: String) = MagnetListFragment().apply {
            arguments = Bundle().apply {
                putString(C.BundleKey.Key_1, keyword)
                putString(C.BundleKey.Key_2, loaderKey)
            }
        }
    }

}