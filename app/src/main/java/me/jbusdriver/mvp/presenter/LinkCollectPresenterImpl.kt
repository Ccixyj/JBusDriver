package me.jbusdriver.mvp.presenter

import me.jbusdriver.mvp.LinkCollectContract
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.ui.data.collect.ICollect

class LinkCollectPresenterImpl(collector: ICollect<ILink>) : BaseAbsCollectPresenter<LinkCollectContract.LinkCollectView, ILink>(collector), LinkCollectContract.LinkCollectPresenter