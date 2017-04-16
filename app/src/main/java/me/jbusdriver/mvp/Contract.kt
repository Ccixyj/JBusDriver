package me.jbusdriver.mvp

import com.cfzx.mvp.view.BaseView
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.bean.MovieDetail
import me.jbusdriver.mvp.presenter.BasePresenter
import me.jbusdriver.ui.data.DataSourceType

/**
 * Created by Administrator on 2017/4/9.
 */
interface MainContract {
    interface MainView : BaseView
    interface MainPresenter : BasePresenter<MainView>
}

interface MovieListContract {
    interface MovieListView : BaseView.BaseListWithRefreshView {
        val type: DataSourceType
    }

    interface MovieListPresenter : BasePresenter.BaseRefreshLoadMorePresenter<MovieListView> {
        fun loadAll(iaAll: Boolean)
    }
}

interface MovieDetailContract {
    interface MovieDetailView : BaseView {
        val movie: Movie
        val detailMovieFromDisk: MovieDetail?
    }

    interface MovieDetailPresenter : BasePresenter<MovieDetailView>, BasePresenter.RefreshPresenter {
        fun loadDetail()
    }
}