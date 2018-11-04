package me.jbusdriver.component.magnet.mvp.presenter

import me.jbusdriver.base.mvp.presenter.BasePresenterImpl
import me.jbusdriver.component.magnet.mvp.MagnetPagerContract.MagnetPagerPresenter
import me.jbusdriver.component.magnet.mvp.MagnetPagerContract.MagnetPagerView

class MagnetPagerPresenterImpl : BasePresenterImpl<MagnetPagerView>(), MagnetPagerPresenter {
    override fun lazyLoad() {
        onFirstLoad()
    }
}