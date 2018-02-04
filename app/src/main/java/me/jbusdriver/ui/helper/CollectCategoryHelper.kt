package me.jbusdriver.ui.helper

import me.jbusdriver.db.bean.ICollectCategory

/**
 * Created by Administrator on 2017/12/3.
 */
class CollectCategoryHelper<T : ICollectCategory> {
//
//      private var isInit = false
//
//
//    fun getCollectGroup(): Map<Category, List<T>> {
//        if (!isInit) error("must call initFromData first")
//        return collectGroupMap
//    }
//
//    fun getDataWrapper(): List<CollectLinkWrapper<T>> {
//        if (!isInit) error("must call initFromData first")
//        return dataWrapperList
//    }
//
//
//    fun getDelegate(): CollectMultiTypeDelegate<T> {
//        if (!isInit) error("must call initFromData first")
//        return adapterDelegate
//    }
//
//    /**
//     * @param parentType in [1..10]
//     */
//    fun initFromData(data: List<T>, parentType: Int) {
//        if (AppConfiguration.enableCategory) {
//            collectGroupMap.clear()
//            dataWrapperList.clear()
//            //添加所有分类
//            CategoryService.queryCategoryTreeLike(parentType).forEach {
//                collectGroupMap[it] = emptyList()
//            }
//            if (!data.isEmpty()) collectGroupMap.putAll(data.groupBy { it.categoryId }.mapKeys { CategoryService.getById(it.key) })
//
//            collectGroupMap.forEach {
//                dataWrapperList.add(CollectLinkWrapper<T>(it.key).apply {
//                    it.value.forEach {
//                        addSubItem(CollectLinkWrapper(null, it).apply {
//                            // delegate.registerItemType(level, R.layout.layout_actress_item) //默认注入类型0，即actress
//                            adapterDelegate.needInjectType.add(level)
//                        })
//                    }
//                    adapterDelegate.needInjectType.add(level)
//                })
//
//            }
//        } else {
//            data.mapTo(dataWrapperList) { CollectLinkWrapper(null, it) }
//        }
//
//        isInit = true
//    }



}