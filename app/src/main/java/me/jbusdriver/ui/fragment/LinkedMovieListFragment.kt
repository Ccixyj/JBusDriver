package me.jbusdriver.ui.fragment

import android.graphics.Paint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_actress_attr.view.*
import kotlinx.android.synthetic.main.layout_load_all.view.*
import me.jbusdriver.common.*
import me.jbusdriver.mvp.LinkListContract
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.mvp.presenter.LinkAbsPresenterImpl
import me.jbusdriver.mvp.presenter.MovieLinkPresenterImpl
import me.jbusdriver.ui.activity.SearchResultActivity
import me.jbusdriver.ui.data.collect.ActressCollector
import me.jbusdriver.ui.data.collect.LinkCollector
import me.jbusdriver.ui.data.collect.MovieCollector


/**
 * ilink 由跳转链接进入的 /历史记录
 */
class LinkedMovieListFragment : AbsMovieListFragment(), LinkListContract.LinkListView {
    private val link by lazy { arguments.getSerializable(C.BundleKey.Key_1)  as? ILink ?: error("no link data ") }
    private val isSearch by lazy { link is SearchLink && activity != null && activity is SearchResultActivity }
    private val isHistory by lazy { arguments.getBoolean(C.BundleKey.Key_2, false) }
    private val attrViews by lazy { mutableListOf<View>() }


    private var collectMenu: MenuItem? = null
    private var removeCollectMenu: MenuItem? = null
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        val isCollect by lazy {
            when (link) {
                is Movie -> MovieCollector.has(link as Movie)
                is ActressInfo -> ActressCollector.has(link as ActressInfo)
                else -> LinkCollector.has(link)
            }

        }
        if (!isHistory || link !is PageLink) { //历史记录隐藏
            collectMenu = menu?.add(Menu.NONE, R.id.action_add_movie_collect, 10, "收藏")?.apply {
                setIcon(R.drawable.ic_star_border_white_24dp)
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                isVisible = !isCollect
            }
            removeCollectMenu = menu?.add(Menu.NONE, R.id.action_remove_movie_collect, 10, "取消收藏")?.apply {
                setIcon(R.drawable.ic_star_white_24dp)
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                isVisible = isCollect
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_add_movie_collect -> {
                //收藏
                KLog.d("收藏")
                val res = when (link) {
                    is Movie -> MovieCollector.addToCollect(link as Movie)
                    is ActressInfo -> ActressCollector.addToCollect((link as ActressInfo).apply {
                        tag = null
                    })
                    else -> LinkCollector.addToCollect(link)
                }
                if (res) {
                    collectMenu?.isVisible = false
                    removeCollectMenu?.isVisible = true
                }
            }
            R.id.action_remove_movie_collect -> {
                //取消收藏
                KLog.d("取消收藏")
                val res = when (link) {
                    is Movie -> MovieCollector.removeCollect(link as Movie)
                    is ActressInfo -> ActressCollector.removeCollect(link as ActressInfo)
                    else -> LinkCollector.removeCollect(link)
                }
                if (res) {
                    collectMenu?.isVisible = true
                    removeCollectMenu?.isVisible = false
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun initData() {
        if (isSearch) {
            RxBus.toFlowable(SearchWord::class.java).subscribeBy{ sea ->
                (mBasePresenter as? LinkAbsPresenterImpl<*>)?.let {
                    (it.linkData as SearchLink).query = sea.query
                    it.onRefresh()
                }
            }.addTo(rxManager)
        }
    }

    override fun gotoSearchResult(query: String) {
        (mBasePresenter as?  LinkAbsPresenterImpl<*>)?.let {
            if (isSearch) {
//                it.linkData.query = query
//                it.onRefresh()
                viewContext.toast("新搜索 : $query")
                RxBus.post(SearchWord(query))
            } else {
                super.gotoSearchResult(query)
            }
        }
    }

    override fun createPresenter() = MovieLinkPresenterImpl(link, arguments.getBoolean(LinkableListFragment.MENU_SHOW_ALL, false) , isHistory)

    override fun <T> showContent(data: T?) {
        KLog.d("parse res :$data")
        if (data is String) {
            getLoaAllView(data)?.let { attrViews.add(it) }
        }

        if (data is IAttr) {
            attrViews.add(getMovieAttrView(data))
        }
    }

    override fun showContents(datas: List<*>?) {
        adapter.removeAllHeaderView()
        attrViews.forEach { adapter.addHeaderView(it) }
        attrViews.clear()
        super.showContents(datas)

    }

    private fun getMovieAttrView(data: IAttr): View = when (data) {
        is ActressAttrs -> {
            this.viewContext.inflate(R.layout.layout_actress_attr).apply {
                //img
                GlideApp.with(this@LinkedMovieListFragment).load(data.imageUrl.toGlideUrl).into(GlideDrawableImageViewTarget(this.iv_actress_avatar))
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

    private fun getLoaAllView(data: String): View? {
        return data.split("：").let { txts ->
            if (txts.size == 2) {
                this.viewContext.inflate(R.layout.layout_load_all).apply {
                    tv_info_title.text = txts[0]
                    val spans = txts[1].split("，")
                    require(spans.size == 2)
                    tv_change_a.text = spans[0]
                    tv_change_b.text = spans[1]
                    tv_change_b.paintFlags = tv_change_b.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                    tv_change_b.setOnClickListener {
                        val showAll = arguments?.getBoolean(MENU_SHOW_ALL) ?: false
                        mBasePresenter?.loadAll(!showAll)
                        arguments?.putBoolean(MENU_SHOW_ALL, !showAll)
                    }
                }
            } else null
        }

    }


    private fun generateTextView() = TextView(this.viewContext).apply {
        textSize = 11.5f
        setTextColor(resources.getColor(R.color.secondText))
    }


    /*================================================*/

    companion
    object {
        //电影列表,演员,链接,搜索入口
        fun newInstance(link: ILink, cancelLazyLoad: Boolean? = null) = LinkedMovieListFragment().apply {
            if (true == cancelLazyLoad) userVisibleHint = true
            arguments = Bundle().apply {
                putSerializable(C.BundleKey.Key_1, link)
            }
        }
    }
    /*================================================*/
}