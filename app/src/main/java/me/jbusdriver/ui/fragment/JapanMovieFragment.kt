package me.jbusdriver.ui.fragment

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.reactivex.Flowable
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_recycle.*
import me.jbusdriver.common.AppBaseFragment
import me.jbusdriver.mvp.JapanMovieContract
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.presenter.JapanMoviePresenterImpl

/**
 * Created by Administrator on 2017/4/9.
 */
class JapanMovieFragment : AppBaseFragment<JapanMovieContract.JapanMoviePresenter, JapanMovieContract.JapanMovieView>(), JapanMovieContract.JapanMovieView {
    override fun createPresenter() = JapanMoviePresenterImpl()

    override val layoutId: Int = R.layout.layout_swipe_recycle

    val adapter = object : BaseQuickAdapter<Movie, BaseViewHolder>(0, mutableListOf<Movie>()) {
        override fun convert(helper: BaseViewHolder, item: Movie) {

        }
    }

    override fun initWidget(rootView: View) {
        adapter.setOnLoadMoreListener({
            mBasePresenter?.onLoadMore()
        }, rv_recycle)
    }

    /*================================================*/


    override fun showContents(datas: List<*>?) {
        adapter.setNewData(datas as MutableList<Movie>?)
        adapter.loadMoreComplete()
    }

    override fun loadComplete() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getRequestParams(page: Int): Flowable<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enableRefresh(b: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun resetList() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        fun newInstance() = JapanMovieFragment()
    }
}