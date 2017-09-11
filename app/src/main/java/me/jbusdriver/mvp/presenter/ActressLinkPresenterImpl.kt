package me.jbusdriver.mvp.presenter

import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.ILink
import org.jsoup.nodes.Document

/**
 * Created by Administrator on 2017/7/29.
 */
class ActressLinkPresenterImpl(val link: ILink) : LinkAbsPresenterImpl<ActressInfo>(link) {

    override fun stringMap(str: Document) = ActressInfo.parseActressList(str)


}