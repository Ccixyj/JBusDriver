package me.jbusdriver.component.magnet.mvp

import me.jbusdriver.base.mvp.BaseView
import me.jbusdriver.base.mvp.presenter.BasePresenter

interface MagnetPagerContract {
    interface MagnetPagerView : BaseView
    interface MagnetPagerPresenter : BasePresenter<MagnetPagerView>, BasePresenter.LazyLoaderPresenter
}


interface MagnetListContract {
    interface MagnetListView : BaseView.BaseListWithRefreshView
    interface MagnetListPresenter : BasePresenter.BaseRefreshLoadMorePresenter<MagnetListView>, BasePresenter.LazyLoaderPresenter{
        fun fetchMagLink(url:String):String
    }
}

