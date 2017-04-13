package me.jbusdriver.ui.fragment

import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.common.AppBaseRecycleFragment
import me.jbusdriver.mvp.AllJapanMovieContract
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.presenter.AllJapanMoviePresenterImpl

/**
 * Created by Administraor on 2017/4/9.
 */
class AllJapanMovieFragment : AppBaseRecycleFragment<AllJapanMovieContract.AllJapanMoviePresenter, AllJapanMovieContract.AllJapanMovieView, Movie>(), AllJapanMovieContract.AllJapanMovieView {
    override fun createPresenter() = AllJapanMoviePresenterImpl()

    override val layoutId: Int = R.layout.layout_swipe_recycle

    override val swipeView: SwipeRefreshLayout  by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager  by lazy { LinearLayoutManager(viewContext) }
    override val adapter: BaseQuickAdapter<Movie, in BaseViewHolder> = object : BaseQuickAdapter<Movie, BaseViewHolder>(R.layout.layout_movie) {
        override fun convert(helper: BaseViewHolder, item: Movie) {
            helper.setText(R.id.tv_movie_title, item.title)
                    .setText(R.id.tv_movie_date, item.date)
                    .setText(R.id.tv_movie_code, item.code)

            Glide.with(this@AllJapanMovieFragment).load(item.imageUrl).dontAnimate().placeholder(R.mipmap.ic_place_holder)
                    .error(R.mipmap.ic_place_holder).centerCrop().into(helper.getView(R.id.iv_movie_img))

            item.tags.map {
                tag ->
                (mLayoutInflater.inflate(R.layout.tv_movie_tag, null) as TextView).let {
                    it.text = tag
                    helper.getView<LinearLayout>(R.id.ll_movie_hot).addView(it)
                }
            }
        }
    }

    /*================================================*/


    companion object {
        fun newInstance() = AllJapanMovieFragment()
    }

}