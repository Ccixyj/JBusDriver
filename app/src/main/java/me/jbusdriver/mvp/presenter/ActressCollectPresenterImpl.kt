package me.jbusdriver.mvp.presenter

import me.jbusdriver.mvp.ActressCollectContract
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.ui.data.collect.ICollect

class ActressCollectPresenterImpl(collector: ICollect<ActressInfo>) : BaseAbsCollectPresenter<ActressCollectContract.ActressCollectView, ActressInfo>(collector), ActressCollectContract.ActressCollectPresenter {

    override fun lazyLoad() {
        onFirstLoad()
    }
}