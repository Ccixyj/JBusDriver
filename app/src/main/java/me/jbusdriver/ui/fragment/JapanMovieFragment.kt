package me.jbusdriver.ui.fragment

import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.common.AppBaseRecycleFragment
import me.jbusdriver.mvp.JapanMovieContract
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.presenter.JapanMoviePresenterImpl

/**
 * Created by Administraor on 2017/4/9.
 */
class JapanMovieFragment : AppBaseRecycleFragment<JapanMovieContract.JapanMoviePresenter, JapanMovieContract.JapanMovieView, Movie>(), JapanMovieContract.JapanMovieView {
    override fun createPresenter() = JapanMoviePresenterImpl()

    override val layoutId: Int = R.layout.layout_swipe_recycle

    override val swipeView: SwipeRefreshLayout  by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager  by lazy { LinearLayoutManager(viewContext) }
    override val adapter: BaseQuickAdapter<Movie, in BaseViewHolder> = object : BaseQuickAdapter<Movie, BaseViewHolder>(android.R.layout.simple_list_item_1) {
        override fun convert(helper: BaseViewHolder?, item: Movie?) {
            helper?.setText(android.R.id.text1, item?.title)
        }
    }


    /*================================================*/


    companion object {
        fun newInstance() = JapanMovieFragment()
    }

}