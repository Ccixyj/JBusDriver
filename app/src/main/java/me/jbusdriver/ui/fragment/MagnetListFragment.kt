package me.jbusdriver.ui.fragment

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
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.base.*
import me.jbusdriver.base.common.AppBaseRecycleFragment
import me.jbusdriver.base.common.C
import me.jbusdriver.mvp.MagnetListContract
import me.jbusdriver.mvp.bean.Magnet
import me.jbusdriver.mvp.presenter.MagnetListPresenterImpl
import me.jbusdriver.ui.adapter.BaseAppAdapter
import me.jbusdriver.ui.data.magnet.MagnetFormatPrefix
import org.jsoup.Jsoup

class MagnetListFragment : AppBaseRecycleFragment<MagnetListContract.MagnetListPresenter, MagnetListContract.MagnetListView, Magnet>(), MagnetListContract.MagnetListView {

    private val keyword by lazy { arguments?.getString(C.BundleKey.Key_1) ?: error("need keyword") }
    private val magnetLoaderKey by lazy {
        arguments?.getString(C.BundleKey.Key_2) ?: error("need magnet loaderKey")
    }

    override fun createPresenter() = MagnetListPresenterImpl(magnetLoaderKey, keyword)

    override val layoutId: Int = R.layout.layout_swipe_recycle
    override val swipeView: SwipeRefreshLayout?  by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager  by lazy { LinearLayoutManager(viewContext) }


    override val adapter: BaseQuickAdapter<Magnet, in BaseViewHolder> by lazy {

        object : BaseAppAdapter<Magnet, BaseViewHolder>(R.layout.layout_magnet_item) {

            override fun convert(helper: BaseViewHolder, item: Magnet) {
                KLog.d("convert $item")
                helper.setText(R.id.tv_magnet_title, item.name)
                        .setText(R.id.tv_magnet_date, item.date)
                        .setText(R.id.tv_magnet_size, item.size)
                        .addOnClickListener(R.id.iv_magnet_copy)
            }

        }.apply {

            fun tryGetMagnet(url: String): Flowable<String> {
                return Flowable.just(url).flatMap { url ->
                    if (url.startsWith(MagnetFormatPrefix)) {
                        Flowable.just(url)
                    } else {
                        Flowable.fromCallable { Jsoup.connect(url).get().select(".content .magnet").text().trim() }
                                .addUserCase(sec = 6)
                                .onErrorReturn { url }
                    }
                }
            }

            setOnItemClickListener { adapter, _, position ->
                (adapter.data.getOrNull(position) as? Magnet)?.let { magnet ->
                    KLog.d("setOnItemClickListener $magnet")
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
                        R.id.iv_magnet_copy -> {

                            tryGetMagnet(magnet.link).compose(SchedulersCompat.io()).subscribeBy { url->
                                this@MagnetListFragment.adapter.setData(position, magnet.copy(link = url))
                                view.context.apply {
                                    copy(url)
                                    toast("复制成功")
                                }
                                KLog.d("copy value : ${view.context.paste()}")
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