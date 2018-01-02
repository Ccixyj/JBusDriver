package me.jbusdriver.ui.helper

import com.chad.library.adapter.base.util.MultiTypeDelegate
import me.jbusdriver.db.bean.Category
import me.jbusdriver.db.bean.ICollectCategory
import me.jbusdriver.db.service.CategoryService
import me.jbusdriver.mvp.bean.CollectLinkWrapper
import me.jbusdriver.ui.data.AppConfiguration
import java.util.concurrent.ConcurrentSkipListSet

/**
 * Created by Administrator on 2017/12/3.
 */
class CollectCategoryHelper<T : ICollectCategory> {

    private var isInit = false
    private val collectGroupMap by lazy { mutableMapOf<Category, List<T>>() }
    private val dataWrapperList by lazy { mutableListOf<CollectLinkWrapper<T>>() }
    private val adapterDelegate by lazy { CollectMultiTypeDelegate<T>() }


    fun getCollectGroup(): Map<Category, List<T>> {
        if (!isInit) error("must call initFromData first")
        return collectGroupMap
    }

    fun getDataWrapper(): List<CollectLinkWrapper<T>> {
        if (!isInit) error("must call initFromData first")
        return dataWrapperList
    }


    fun getDelegate(): CollectMultiTypeDelegate<T> {
        if (!isInit) error("must call initFromData first")
        return adapterDelegate
    }

    /**
     * @param parentType in [1..10]
     */
    fun initFromData(data: List<T>, parentType: Int) {
        if (AppConfiguration.enableCategory) {
            collectGroupMap.clear()
            dataWrapperList.clear()
            //添加所有分类
            CategoryService.queryCategoryTreeLike(parentType).forEach {
                collectGroupMap.put(it, emptyList())
            }
            if (!data.isEmpty()) collectGroupMap.putAll(data.groupBy { it.categoryId }.mapKeys { CategoryService.getById(it.key) })

            collectGroupMap.forEach {
                dataWrapperList.add(CollectLinkWrapper<T>(it.key).apply {
                    it.value.forEach {
                        addSubItem(CollectLinkWrapper(null, it).apply {
                            // delegate.registerItemType(level, R.layout.layout_actress_item) //默认注入类型0，即actress
                            adapterDelegate.needInjectType.add(level)
                        })
                    }
                    adapterDelegate.needInjectType.add(level)
                })

            }
        } else {
            data.mapTo(dataWrapperList) { CollectLinkWrapper(null, it) }
        }

        isInit = true
    }


    class CollectMultiTypeDelegate<T : ICollectCategory> : com.chad.library.adapter.base.util.MultiTypeDelegate<CollectLinkWrapper<T>>() {

        /**
         * 待注入的类型
         */
        val needInjectType = ConcurrentSkipListSet<Int>()

        override fun getItemType(t: CollectLinkWrapper<T>): Int {
            require(needInjectType.isEmpty()) { "needInjectType must all inject" }
            return t.level
        }

        override fun registerItemType(type: Int, layoutResId: Int): MultiTypeDelegate<*> {
            needInjectType.remove(type)
            return super.registerItemType(type, layoutResId)
        }

        override fun registerItemTypeAutoIncrease(vararg layoutResIds: Int): MultiTypeDelegate<*> {
            needInjectType.removeAll(0..layoutResIds.size)
            return super.registerItemTypeAutoIncrease(*layoutResIds)
        }
    }

}