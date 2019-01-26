package me.jbusdriver.component.magnet.mvp.bean

import me.jbusdriver.commen.bean.ILink
import me.jbusdriver.commen.bean.db.LinkCategory

data class Magnet(val name: String, val size: String, val date: String, override val link: String) : ILink {
    @Transient
    override var categoryId: Int = LinkCategory.id ?: 10
}