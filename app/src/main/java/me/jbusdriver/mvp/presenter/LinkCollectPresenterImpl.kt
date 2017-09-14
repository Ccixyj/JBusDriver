package me.jbusdriver.mvp.presenter

import me.jbusdriver.mvp.LinkCollectContract
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.ui.data.collect.LinkCollector

class LinkCollectPresenterImpl : BaseAbsCollectPresenter<LinkCollectContract.LinkCollectView, ILink>(), LinkCollectContract.LinkCollectPresenter {

    override val collector by lazy { LinkCollector }
}