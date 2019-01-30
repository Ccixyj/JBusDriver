package me.jbusdriver.mvp.presenter

import me.jbusdriver.mvp.ActressCollectContract
import me.jbusdriver.mvp.bean.ActressInfo

class ActressCollectPresenterImpl : BaseAbsCollectPresenter<ActressCollectContract.ActressCollectView, ActressInfo>(),
    ActressCollectContract.ActressCollectPresenter {

    override fun lazyLoad() {
        onFirstLoad()
    }
}