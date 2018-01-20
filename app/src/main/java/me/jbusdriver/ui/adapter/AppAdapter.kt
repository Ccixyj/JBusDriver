package me.jbusdriver.ui.adapter

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import me.jbusdriver.common.AppContext
import me.jbusdriver.common.KLog

abstract class BaseAppAdapter<T, K : BaseViewHolder> : BaseQuickAdapter<T, K> {

    private val Tag by lazy { this::class.java.name ?: error("must have a class name") }

    constructor(layoutResId: Int, data: MutableList<T>?) : super(layoutResId, data)
    constructor(data: MutableList<T>?) : super(data)
    constructor(layoutResId: Int) : super(layoutResId)

    override fun bindToRecyclerView(recyclerView: RecyclerView?) {
        recyclerView?.let {
            (it as? LinearLayoutManager)?.recycleChildrenOnDetach = true
            it.recycledViewPool = AppContext.instace.recycledViewPoolHolder.getOrPut(Tag) {
                RecyclerView.RecycledViewPool()
            }
        }
        KLog.t(Tag).d("bindToRecyclerView ${AppContext.instace.recycledViewPoolHolder.size} : ${AppContext.instace.recycledViewPoolHolder.keys.joinToString()}")
        super.bindToRecyclerView(recyclerView)
    }

}

abstract class BaseMultiItemAppAdapter<T : MultiItemEntity, K : BaseViewHolder> : BaseMultiItemQuickAdapter<T, K> {

    private val Tag by lazy { this::class.java.name ?: error("must have a class name") }

    constructor(data: List<T>?) : super(data)


    override fun bindToRecyclerView(recyclerView: RecyclerView?) {
        recyclerView?.let {
            (it as? LinearLayoutManager)?.recycleChildrenOnDetach = true
            it.recycledViewPool = AppContext.instace.recycledViewPoolHolder.getOrPut(Tag) {
                RecyclerView.RecycledViewPool()
            }
        }
        KLog.t(Tag).d("bindToRecyclerView ${AppContext.instace.recycledViewPoolHolder.size} : ${AppContext.instace.recycledViewPoolHolder.keys.joinToString()}")
        super.bindToRecyclerView(recyclerView)
    }

}