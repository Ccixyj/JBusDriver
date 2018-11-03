package me.jbusdriver.mvp.bean

import com.chad.library.adapter.base.entity.AbstractExpandableItem
import me.jbusdriver.db.bean.Category
import me.jbusdriver.commen.bean.ICollectCategory
import me.jbusdriver.db.service.CategoryService

/**
 * Created by Administrator on 2017/9/26 0026.
 */

class CollectLinkWrapper<T : ICollectCategory>(private val categoryDec: Category? = null, val linkBean: T? = null) : AbstractExpandableItem<CollectLinkWrapper<T>>() {


    val category by lazy {
        categoryDec ?: CategoryService.getById(subItems?.firstOrNull()?.linkBean?.categoryId
                ?: error("category exist and  id must > 0"))
        ?: error("category exist and  id must > 0")
    }

    override fun getLevel(): Int {
        return if (linkBean == null) {
            //菜单
            category.depth.let {
                if (it in (0..1)) 0 else it
            }
        } else {
            -1 //原item
        }
    }
}