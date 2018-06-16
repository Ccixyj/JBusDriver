package me.jbusdriver.mvp.presenter

import com.chad.library.adapter.base.util.MultiTypeDelegate
import me.jbusdriver.db.bean.Category
import me.jbusdriver.db.bean.ICollectCategory
import me.jbusdriver.mvp.bean.CollectLinkWrapper
import java.util.concurrent.ConcurrentSkipListSet

interface BaseCollectPresenter<T : ICollectCategory> {

        val collectGroupMap: MutableMap<Category, List<T>>
        val adapterDelegate: CollectMultiTypeDelegate<T>


        fun setCategory(t: T, category: Category)

        class CollectMultiTypeDelegate<T : ICollectCategory> : com.chad.library.adapter.base.util.MultiTypeDelegate<CollectLinkWrapper<T>>() {

            /**
             * 待注入的类型
             */
             val needInjectType = ConcurrentSkipListSet<Int>()

            override fun getItemType(t: CollectLinkWrapper<T>): Int {
                //require(needInjectType.isEmpty() || this. ) { "needInjectType must all inject" }
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