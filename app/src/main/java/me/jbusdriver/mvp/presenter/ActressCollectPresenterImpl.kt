package me.jbusdriver.mvp.presenter

import me.jbusdriver.ui.data.CollectManager
import me.jbusdriver.mvp.ActressCollectContract
import me.jbusdriver.mvp.bean.ActressInfo

class ActressCollectPresenterImpl : BaseAbsCollectPresenter<ActressCollectContract.ActressCollectView,ActressInfo>(), ActressCollectContract.ActressCollectPresenter {

    override fun onFirstLoad() {
    }

    override fun getData() = CollectManager.actress_data

    override fun onStart(firstStart: Boolean) {
        super.onStart(firstStart)
        onRefresh()
    }
}