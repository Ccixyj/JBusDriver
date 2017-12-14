package me.jbusdriver.ui.helper

import me.jbusdriver.db.bean.AllFirstParentDBCategoryGroup
import me.jbusdriver.db.bean.Category
import me.jbusdriver.db.bean.ICollectCategory
import me.jbusdriver.db.service.CategoryService

/**
 * Created by Administrator on 2017/12/3.
 */
class CollectCategoryHelper<T : ICollectCategory> {

    private var isInit = false
    private val collectGroupMap by lazy { mutableMapOf<Category, List<T>>() }
    private val allf by lazy { AllFirstParentDBCategoryGroup }

    fun getCollectGroup(): Map<Category, List<T>> {
        if (!isInit) error("must call initFromData first")
        return collectGroupMap
    }

    /**
     * @param parentType in [1..10]
     */
    fun initFromData(data: List<T>, parentType: Int) {
        collectGroupMap.clear()
        isInit = true
        //添加所有分类
        CategoryService.queryCategoryTreeLike(parentType).forEach {
            collectGroupMap.put(it, emptyList())
        }
        if (data.isEmpty()) return
        collectGroupMap.putAll(data.groupBy { it.categoryId }.mapKeys { CategoryService.getById(it.key) })
    }
}