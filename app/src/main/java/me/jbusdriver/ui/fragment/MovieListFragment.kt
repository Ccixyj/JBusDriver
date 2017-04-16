package me.jbusdriver.ui.fragment

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.listener.SimpleClickListener
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.common.AppBaseRecycleFragment
import me.jbusdriver.common.KLog
import me.jbusdriver.common.dpToPx
import me.jbusdriver.mvp.MovieListContract
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.presenter.MovieListPresenterImpl
import me.jbusdriver.ui.activity.MovieDetailActivity
import me.jbusdriver.ui.data.DataSourceType


/**
 * Created by Administraor on 2017/4/9.
 */
class MovieListFragment : AppBaseRecycleFragment<MovieListContract.MovieListPresenter, MovieListContract.MovieListView, Movie>(), MovieListContract.MovieListView {
    override fun createPresenter() = MovieListPresenterImpl()

    override val layoutId: Int = R.layout.layout_swipe_recycle

    override val swipeView: SwipeRefreshLayout  by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager  by lazy { LinearLayoutManager(viewContext) }
    override val adapter: BaseQuickAdapter<Movie, in BaseViewHolder> = object : BaseQuickAdapter<Movie, BaseViewHolder>(R.layout.layout_movie) {
        val padding by lazy { this@MovieListFragment.viewContext.dpToPx(8f) }
        val colors = listOf(0xff2195f3.toInt(), 0xff4caf50.toInt(), 0xffff0030.toInt()) //蓝,绿,红

        val lp by lazy {
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, this@MovieListFragment.viewContext.dpToPx(24f)).apply {
                leftMargin = padding
                gravity = Gravity.CENTER_VERTICAL
            }
        }

        override fun convert(helper: BaseViewHolder, item: Movie) {
            helper.setText(R.id.tv_movie_title, item.title)
                    .setText(R.id.tv_movie_date, item.date)
                    .setText(R.id.tv_movie_code, item.code)

            Glide.with(this@MovieListFragment).load(item.imageUrl).placeholder(R.drawable.ic_place_holder)
                    .error(R.drawable.ic_place_holder).centerCrop().into(helper.getView(R.id.iv_movie_img))


            with(helper.getView<LinearLayout>(R.id.ll_movie_hot)) {
                KLog.d("tags : ${item.tags}")
                this.removeAllViews()
                item.tags.mapIndexed { index, tag ->
                    (mLayoutInflater.inflate(R.layout.tv_movie_tag, null) as TextView).let {
                        it.text = tag
                        it.setPadding(padding, 0, padding, 0)
                        (it.background as? GradientDrawable)?.setColor(colors.getOrNull(index % 3) ?: colors.first())
                        it.layoutParams = lp
                        this.addView(it)
                    }
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initWidget(rootView: View) {
        super.initWidget(rootView)
        recycleView.addOnItemTouchListener(object : SimpleClickListener() {


            override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                Toast.makeText(viewContext, "" + Integer.toString(position), Toast.LENGTH_SHORT).show()
                (adapter.data.getOrNull(position) as? Movie)?.let { MovieDetailActivity.start(activity,it) }
            }

            override fun onItemLongClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
            }

            override fun onItemChildClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                Toast.makeText(viewContext, "" + Integer.toString(position), Toast.LENGTH_SHORT).show()
            }

            override fun onItemChildLongClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                Toast.makeText(viewContext, "" + Integer.toString(position), Toast.LENGTH_SHORT).show()
            }
        })

    }

    /*================================================*/
    override val type by lazy { arguments.getSerializable("type") as? DataSourceType ?: DataSourceType.CENSORED }

    companion object {
        fun newInstance(type: DataSourceType) = MovieListFragment().apply {
            arguments = Bundle().apply { putSerializable("type", type) }
        }
    }

}