package me.jbusdriver.mvp.bean

import com.chad.library.adapter.base.entity.AbstractExpandableItem
import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 * Created by Administrator on 2017/9/26 0026.
 */

class ActressWrapper(val actressInfo: ActressInfo? = null) : AbstractExpandableItem<ActressWrapper>(), MultiItemEntity {

    override fun getItemType(): Int = actressInfo?.category?.depth ?: subItems.first().itemType +1  //如果是分类项　depth+１　表示类型　和默认项区分开来

    override fun getLevel(): Int = if (itemType == 0) Int.MAX_VALUE else itemType
    override fun toString(): String {
        return "ActressWrapper(getItemType =$itemType , level = $level actressInfo=$actressInfo , subItems = $subItems ) "
    }


}