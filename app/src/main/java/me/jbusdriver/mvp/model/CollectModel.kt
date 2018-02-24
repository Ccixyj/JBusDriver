package me.jbusdriver.mvp.model

import me.jbusdriver.common.appContext
import me.jbusdriver.common.toast
import me.jbusdriver.db.bean.LinkItem
import me.jbusdriver.db.service.LinkService
import me.jbusdriver.ui.data.collect.ICollect

/**
 * Created by Administrator on 2018/2/13.
 */
object CollectModel : ICollect<LinkItem> {

    override val key: String = "CollectModel"


    override val dataList: MutableList<LinkItem>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun addToCollect(data: LinkItem): Boolean {
        LinkService.saveOrUpdate(listOf(data))
        appContext.toast("收藏成功")
        return true
    }

    override fun has(data: LinkItem) = LinkService.hasByKey(data) >= 1

    override fun removeCollect(data: LinkItem) = LinkService.remove(data).also {
        appContext.toast("已经取消收藏")
    }

    override fun reload() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(data: LinkItem) = LinkService.update(data)
}