package me.jbusdriver.ui.fragment

import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.loadmore.LoadMoreView
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.base.KLog
import me.jbusdriver.base.urlHost
import me.jbusdriver.base.urlPath
import me.jbusdriver.base.common.AppBaseRecycleFragment
import me.jbusdriver.base.glide.toGlideNoHostUrl
import me.jbusdriver.http.JAVBusService.Companion.defaultFastUrl
import me.jbusdriver.mvp.HotRecommendContract
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.RecommendRespBean
import me.jbusdriver.mvp.presenter.HotRecommendPresenterImpl
import me.jbusdriver.ui.activity.MovieDetailActivity
import me.jbusdriver.ui.activity.MovieListActivity

/**
 * Created by Administrator on 2017/7/30.
 */
class RecommendListFragment : AppBaseRecycleFragment<HotRecommendContract.HotRecommendPresenter, HotRecommendContract.HotRecommendView, RecommendRespBean>(), HotRecommendContract.HotRecommendView {


    override fun createPresenter() = HotRecommendPresenterImpl()

    override val layoutId: Int = R.layout.layout_swipe_recycle
    override val swipeView: SwipeRefreshLayout?  by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager  by lazy { LinearLayoutManager(viewContext) }

    override val adapter = object : BaseQuickAdapter<RecommendRespBean, BaseViewHolder>(R.layout.layout_recommend_item) {

        override fun convert(helper: BaseViewHolder, item: RecommendRespBean) {
//            val images = defaultImageUrlHosts[if (item.key.img.endsWith("xyz")) "xyz" else "default"]?.map { it +  }
//                    ?: emptyList()
//            val image = images.shuffled().firstOrNull() ?: ""
            Glide.with(viewContext).load(item.key.img.toGlideNoHostUrl).into(helper.getView(R.id.iv_recommend_img))
            helper.setText(R.id.tv_recommend_title, item.key.name)
                    .setText(R.id.tv_reason, if (item.reason?.isNotBlank() == true) "推荐理由：${item.reason}" else "")
                    .setText(R.id.tv_recommend_score, item.score.toString())


        }

    }

    override fun initWidget(rootView: View) {
        super.initWidget(rootView)
        adapter.setOnLoadMoreListener({
            KLog.d("onLoadMore")
            mBasePresenter?.onLoadMore()

        }, recycleView)
        adapter.setLoadMoreView(object : LoadMoreView() {
            override fun getLayoutId(): Int = R.layout.layout_load_reset

            override fun getLoadingViewId(): Int = R.id.tv_end

            override fun getLoadEndViewId(): Int = R.id.tv_end

            override fun getLoadFailViewId(): Int = R.id.tv_end
        })
        adapter.setOnItemClickListener { _, view, position ->
            adapter.getItem(position)?.let {
                KLog.d(it.key)
//                val image = defaultImageUrlHosts[if (it.key.img.endsWith("xyz")) "xyz" else "default"]?.map { h -> h + it.key.img } ?: emptyList()
                val xyz = it.key.url.urlHost.endsWith("xyz")
                val needChange = !xyz && it.key.url.urlHost != defaultFastUrl
                val url = if (needChange) defaultFastUrl + it.key.url.urlPath else it.key.url
                if (it.key.url.contains("/star/", false)) {
                    MovieListActivity.start(viewContext, ActressInfo(it.key.name, it.key.img, url))
                } else {
//                    SearchResultActivity.start(this.viewContext, it.key.name.split(" ").component1())
                    MovieDetailActivity.start(this.viewContext, url)
                }
            }
        }
    }

    override fun showContents(data: List<*>) {
        val size =  adapter.data.size
        adapter.data .clear()
        adapter.notifyItemRangeRemoved(0,size)
        super.showContents(data)
    }

    override fun loadMoreEnd(clickable: Boolean) {
        super.loadMoreEnd(true)
    }

    companion object {
        fun newInstance() = RecommendListFragment()

    }

}