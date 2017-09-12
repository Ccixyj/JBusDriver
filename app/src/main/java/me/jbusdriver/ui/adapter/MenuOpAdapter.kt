package me.jbusdriver.ui.adapter

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import jbusdriver.me.jbusdriver.R

/**
 * Created by Administrator on 2017/9/12 0012.
 */
const val Expand_Type_Head = 0
const val Expand_Type_Item = 1

class MenuOpAdapter(data: List<MultiItemEntity>) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {

    init {
        addItemType(Expand_Type_Head, R.layout.layout_menu_op_head)
        addItemType(Expand_Type_Item, R.layout.layout_menu_op_item)
    }

    override fun convert(helper: BaseViewHolder, item: MultiItemEntity) {
        when (item.itemType) {

        }
    }
}