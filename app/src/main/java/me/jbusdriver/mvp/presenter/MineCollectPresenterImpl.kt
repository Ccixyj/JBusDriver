package me.jbusdriver.mvp.presenter

import me.jbusdriver.base.mvp.presenter.BasePresenterImpl
import me.jbusdriver.mvp.MineCollectContract

class MineCollectPresenterImpl : BasePresenterImpl<MineCollectContract.MineCollectView>(), MineCollectContract.MineCollectPresenter {
    override fun lazyLoad() {
        onFirstLoad()
    }
}