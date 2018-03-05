package me.jbusdriver.mvp.presenter

import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.bean.PageInfo
import org.jsoup.nodes.Document

/**
 * 演员列表
 */
class ActressLinkPresenterImpl(val link: ILink) : LinkAbsPresenterImpl<ActressInfo>(link) {

    override fun stringMap(page: PageInfo, str: Document) = ActressInfo.parseActressList(str)

}