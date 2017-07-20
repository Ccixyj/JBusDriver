package me.jbusdriver.ui.fragment

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.common.*
import me.jbusdriver.mvp.MovieListContract
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.ui.activity.MovieDetailActivity
import me.jbusdriver.ui.activity.SearchResultActivity
import me.jbusdriver.ui.data.DataSourceType


/**
 * ilink 界面解析
 */
abstract class MovieListFragment : AppBaseRecycleFragment<MovieListContract.MovieListPresenter, MovieListContract.MovieListView, Movie>(), MovieListContract.MovieListView {

    override val layoutId: Int = R.layout.layout_swipe_recycle

    override val swipeView: SwipeRefreshLayout?  by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager  by lazy { LinearLayoutManager(viewContext) }
    override val adapter: BaseQuickAdapter<Movie, in BaseViewHolder> = object : BaseQuickAdapter<Movie, BaseViewHolder>(R.layout.layout_movie_item) {
        val padding by lazy { this@MovieListFragment.viewContext.dpToPx(8f) }
        val colors = listOf(0xff2195f3.toInt(), 0xff4caf50.toInt(), 0xffff0030.toInt()) //蓝,绿,红

        val lp by lazy {
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, this@MovieListFragment.viewContext.dpToPx(24f)).apply {
                leftMargin = padding
                gravity = Gravity.CENTER_VERTICAL
            }
        }

        override fun convert(holder: BaseViewHolder, item: Movie) {
            holder.setText(R.id.tv_movie_title, item.title)
                    .setText(R.id.tv_movie_date, item.date)
                    .setText(R.id.tv_movie_code, item.code)

            Glide.with(this@MovieListFragment).load(item.imageUrl).placeholder(R.drawable.ic_place_holder)
                    .error(R.drawable.ic_place_holder).centerCrop().into(holder.getView(R.id.iv_movie_img))


            with(holder.getView<LinearLayout>(R.id.ll_movie_hot)) {
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

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.main, menu)

       menu?.getItem(0)?.let {
           val mSearchView = MenuItemCompat.getActionView(it) as SearchView
           mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
               override fun onQueryTextSubmit(query: String): Boolean {
                   KLog.i("search >> $query")
                   if(TextUtils.isEmpty(query)) viewContext.toast("关键字不能为空!")
                   SearchResultActivity.start(viewContext,query)
                   return true
               }

               override fun onQueryTextChange(newText: String): Boolean {
                   return false
               }
           })
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the CENSORED/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        when (id) {
            R.id.action_show_all -> {
                item.isChecked = !item.isChecked
                if (item.isChecked) item.title = "已发布" else item.title = "全部电影"  /*false : 已发布的 ,true :全部*/
                mBasePresenter?.loadAll(item.isChecked)
            }
            R.id.action_search -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun initWidget(rootView: View) {
        super.initWidget(rootView)
        adapter.setOnItemClickListener { adapter, _, position ->
            (adapter.data.getOrNull(position) as? Movie)?.let { MovieDetailActivity.start(activity, it) }
        }
    }


    /*================================================*/
    override val type by lazy { arguments.getSerializable(C.BundleKey.Key_1) as? DataSourceType ?: DataSourceType.CENSORED }

}