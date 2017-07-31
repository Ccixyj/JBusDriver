package me.jbusdriver.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_actress_attr.view.*
import me.jbusdriver.common.*
import me.jbusdriver.mvp.LinkListContract
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.mvp.presenter.LinkAbsPresenterImpl
import me.jbusdriver.mvp.presenter.MovieLinkPresenterImpl
import me.jbusdriver.ui.activity.SearchResultActivity


/**
 * ilink 界面解析
 */
class LinkMovieListFragment : MovieListFragment(), LinkListContract.LinkListView {
    val link by lazy { arguments.getSerializable(C.BundleKey.Key_1)  as? ILink ?: error("no link data ") }
    private val isSearch by lazy { link is SearchLink && activity != null && activity is SearchResultActivity }

    private val attrViews by lazy { mutableListOf<View>() }

    override fun initData() {
        if (isSearch) {
            RxBus.toFlowable(SearchWord::class.java).subscribeBy({ sea ->
                (mBasePresenter as? LinkAbsPresenterImpl<*>)?.let {
                    (it.linkData as SearchLink).query = sea.query
                    it.onRefresh()
                }
            }).addTo(rxManager)
        }
    }

    override fun gotoSearchResult(query: String) {
        (mBasePresenter as?  LinkAbsPresenterImpl<*>)?.let {
            if (isSearch) {
//                it.linkData.query = query
//                it.onRefresh()
                viewContext.toast(query)
                RxBus.post(SearchWord(query))
            } else {
                super.gotoSearchResult(query)
            }
        }
    }

    override fun createPresenter() = MovieLinkPresenterImpl(link)

    override fun <T> showContent(data: T?) {
        KLog.d("parse res :$data")
        if (data is IAttr) {
            attrViews.clear()
            attrViews.add(getMovieAttrView(data))
        }
    }

    override fun showContents(datas: List<*>?) {
        adapter.removeAllHeaderView()
        attrViews.forEach { adapter.addHeaderView(it) }
        super.showContents(datas)

    }

    private fun getMovieAttrView(data: IAttr): View {
        return when (data) {
            is ActressAttrs -> {
                this.viewContext.inflate(R.layout.layout_actress_attr).apply {
                    //img
                    Glide.with(this@LinkMovieListFragment).load(data.imageUrl).into(GlideDrawableImageViewTarget(this.iv_actress_avatar))
                    //title
                    this.ll_attr_container.addView(generateTextView().apply {
                        textSize = 16f
                        setTextColor(resources.getColor(R.color.primaryText))
                        text = data.title
                    })

                    data.info.forEach {
                        this.ll_attr_container.addView(generateTextView().apply { text = it })
                    }
                }
            }
            else -> error("current not provide for IAttr $data")
        }
    }

    private fun generateTextView() = TextView(this.viewContext).apply {
        textSize = 11.5f
        setTextColor(resources.getColor(R.color.secondText))
    }


    /*================================================*/

    companion object {
        fun newInstance(link: ILink, cancelLazyLoad: Boolean? = null) = LinkMovieListFragment().apply {
            if (true == cancelLazyLoad) userVisibleHint = true
            arguments = Bundle().apply {
                putSerializable(C.BundleKey.Key_1, link)
            }
        }
    }
    /*================================================*/
}