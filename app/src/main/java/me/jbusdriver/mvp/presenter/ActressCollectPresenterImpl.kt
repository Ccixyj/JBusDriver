package me.jbusdriver.mvp.presenter

import me.jbusdriver.mvp.ActressCollectContract
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.ui.data.collect.ActressCollector

class ActressCollectPresenterImpl : BaseAbsCollectPresenter<ActressCollectContract.ActressCollectView, ActressInfo>(), ActressCollectContract.ActressCollectPresenter {
    override val collector by lazy { ActressCollector }
}