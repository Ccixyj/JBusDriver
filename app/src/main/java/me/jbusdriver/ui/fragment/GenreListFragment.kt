package me.jbusdriver.ui.fragment

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import com.xiaofeng.flowlayoutmanager.FlowLayoutManager
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.base.GSON
import me.jbusdriver.base.fromJson
import me.jbusdriver.base.toJsonString
import me.jbusdriver.base.common.AppBaseRecycleFragment
import me.jbusdriver.base.common.C
import me.jbusdriver.mvp.GenreListContract
import me.jbusdriver.mvp.bean.Genre
import me.jbusdriver.mvp.presenter.GenreListPresenterImpl
import me.jbusdriver.ui.adapter.GenreAdapter

/**
 * Created by Administrator on 2017/7/30.
 */
class GenreListFragment : AppBaseRecycleFragment<GenreListContract.GenreListPresenter, GenreListContract.GenreListView, Genre>(), GenreListContract.GenreListView {


    override fun createPresenter() = GenreListPresenterImpl()

    override val layoutId: Int = R.layout.layout_swipe_recycle
    override val swipeView: SwipeRefreshLayout?  by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager  by lazy { FlowLayoutManager().apply { isAutoMeasureEnabled = true } }

    override val adapter =/* object :*/ GenreAdapter()/*{
        override fun convert(holder: BaseViewHolder, item: Genre) {
            holder.getView<TextView>(R.id.tv_movie_genre).layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
            holder.setText(R.id.tv_movie_genre, item.name)
                    .setTextColor(R.id.tv_movie_genre, R.color.primaryText)
            (holder.getView<TextView>(R.id.tv_movie_genre).background as? GradientDrawable)?.apply {
                setColor(context.resources.getColor(android.R.color.transparent))
            }
        }
    }
*/
    override val data by lazy {
        arguments?.getString(C.BundleKey.Key_1)?.let { GSON.fromJson<List<Genre>>(it) }
                ?: emptyList()
    }


    companion object {
        fun newInstance(genres: List<Genre>) = GenreListFragment().apply {
            arguments = Bundle().apply {
                putString(C.BundleKey.Key_1, genres.toJsonString())
            }
        }

    }

}