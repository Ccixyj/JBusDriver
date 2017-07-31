package me.jbusdriver.mvp

import com.cfzx.mvp.view.BaseView
import me.jbusdriver.mvp.bean.Genre
import me.jbusdriver.mvp.bean.Magnet
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.bean.MovieDetail
import me.jbusdriver.mvp.presenter.BasePresenter
import me.jbusdriver.ui.data.DataSourceType
import org.jsoup.nodes.Element

/**
 * Created by Administrator on 2017/4/9.
 */
interface MainContract {
    interface MainView : BaseView
    interface MainPresenter : BasePresenter<MainView>
}

interface LinkListContract {
    interface LinkListView : BaseView.BaseListWithRefreshView {
        val type: DataSourceType
    }

    interface LinkListPresenter : BasePresenter.BaseRefreshLoadMorePresenter<LinkListView> {
        fun loadAll(iaAll: Boolean)
    }
}

interface MovieDetailContract {
    interface MovieDetailView : BaseView {
        val movie: Movie
        val detailMovieFromDisk: MovieDetail?
        fun addMagnet(t: List<Magnet>)
        fun initMagnetLoad()
    }

    interface MovieDetailPresenter : BasePresenter<MovieDetailView>, BasePresenter.RefreshPresenter {
        fun loadDetail()
        fun loadMagnets(doc: Element)
    }
}

interface MovieParseContract {
    interface MovieParseView : BaseView
    interface MovieParsePresenter : BasePresenter<MovieParseView>

}

interface MineCollectContract{
    interface MineCollectView : BaseView
    interface MineCollectPresenter : BasePresenter<MineCollectView>
}

interface MovieCollectContract{
    interface MovieCollectView : BaseView
    interface MovieCollectPresenter : BasePresenter<MovieCollectView>
}

interface ActressCollectContract{
    interface ActressCollectView :  BaseView.BaseListWithRefreshView
    interface ActressCollectPresenter : BasePresenter.BaseRefreshLoadMorePresenter<ActressCollectView>
}

interface GenrePageContract {
    interface GenrePageView :  BaseView {
        val titleValues : MutableList<String>
        val fragmentValues : MutableList<List<Genre>>
    }
    interface GenrePagePresenter : BasePresenter<GenrePageView>
}

interface GenreListContract{
    interface GenreListView :  BaseView.BaseListWithRefreshView{
        val data :List<Genre>
    }
    interface GenreListPresenter : BasePresenter.BaseRefreshLoadMorePresenter<GenreListView>
}
