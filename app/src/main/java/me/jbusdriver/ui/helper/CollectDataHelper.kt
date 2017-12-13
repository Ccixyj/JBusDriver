package me.jbusdriver.ui.helper

import me.jbusdriver.db.bean.Category
import me.jbusdriver.db.service.CategoryService
import me.jbusdriver.mvp.bean.ILink

/**
 * Created by Administrator on 2017/12/3.
 */
class CollectDataHelper<in T : ILink> {

    private var isInit = false
    private val collectGroupMap by lazy { mutableMapOf<Category, List<T>>() }

    private fun getCollectGroup(): Map<Category, List<T>> {
        if (!isInit) error("must call initFromData first")
        return collectGroupMap
    }

    fun initFromData(data: List<T>) {
        collectGroupMap.clear()
        isInit = true
        if (data.isEmpty()) return
//        collectGroupMap.putAll(data.groupBy { it.category })
        //添加其他未使用分类
//        val usedId = actGroupMap.keys.mapNotNull { it.id }
//        CategoryService.queryTreeByLike(2).filterNot { usedId.contains(it.id) }
//                .forEach {
//                    actGroupMap.put(it, emptyList())
//                }
    }
}