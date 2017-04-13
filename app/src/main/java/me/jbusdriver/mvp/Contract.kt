package me.jbusdriver.mvp

import com.cfzx.mvp.view.BaseView
import me.jbusdriver.mvp.presenter.BasePresenter

/**
 * Created by Administrator on 2017/4/9.
 */
interface MainContract {
    interface MainView : BaseView
    interface MainPresenter : BasePresenter<MainView> {
        fun loadFastJAVUrl()
    }


}

interface AllJapanMovieContract {
    interface AllJapanMovieView :BaseView.BaseListWithRefreshView
    interface AllJapanMoviePresenter : BasePresenter.BaseRefreshLoadMorePresenter<AllJapanMovieView>
}