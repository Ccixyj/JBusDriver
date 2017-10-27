package me.jbusdriver.mvp.bean

import com.chad.library.adapter.base.entity.AbstractExpandableItem

/**
 * Created by Administrator on 2017/9/26 0026.
 */

class ActressWrapper(val actressInfo: ActressInfo? = null) : AbstractExpandableItem<ActressWrapper>() {


    override fun getLevel(): Int {
        return if (actressInfo == null) {
            //菜单
            subItems.first().actressInfo?.category?.depth ?: 0
        } else {
            -1
        }
    }

    override fun toString(): String {
        return "ActressWrapper(level = $level actressInfo=$actressInfo , subItems = $subItems ) "
    }


}