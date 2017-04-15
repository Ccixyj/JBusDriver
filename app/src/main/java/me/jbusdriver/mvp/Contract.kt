package me.jbusdriver.mvp

import android.support.v4.util.ArrayMap
import com.cfzx.mvp.view.BaseView
import me.jbusdriver.mvp.presenter.BasePresenter

/**
 * Created by Administrator on 2017/4/9.
 */
interface MainContract {
    interface MainView : BaseView{
        var urls : ArrayMap<String, String>
    }
    interface MainPresenter : BasePresenter<MainView> {
        fun initUrls()

    }


}

interface MovieListContract {
    interface MovieListView : BaseView.BaseListWithRefreshView {
        val source: String?
    }

    interface MovieListPresenter : BasePresenter.BaseRefreshLoadMorePresenter<MovieListView>
}