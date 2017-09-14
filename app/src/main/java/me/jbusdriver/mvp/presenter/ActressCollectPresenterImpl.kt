package me.jbusdriver.mvp.presenter

import me.jbusdriver.mvp.ActressCollectContract
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.ui.data.CollectManager

class ActressCollectPresenterImpl : BaseAbsCollectPresenter<ActressCollectContract.ActressCollectView, ActressInfo>(), ActressCollectContract.ActressCollectPresenter {

    override fun getData() =   CollectManager.actressCache

}