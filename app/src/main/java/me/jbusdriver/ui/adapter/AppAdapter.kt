package me.jbusdriver.ui.adapter

import android.support.v7.widget.RecyclerView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import me.jbusdriver.base.KLog

abstract class BaseAppAdapter<T, K : BaseViewHolder> : BaseQuickAdapter<T, K> {

    constructor(layoutResId: Int, data: MutableList<T>?) : super(layoutResId, data)
    constructor(data: MutableList<T>?) : super(data)
    constructor(layoutResId: Int) : super(layoutResId)

    override fun bindToRecyclerView(recyclerView: RecyclerView?) {
        KLog.i("bindToRecyclerView ")
        super.bindToRecyclerView(recyclerView)
    }


    override fun onViewAttachedToWindow(holder: K) {
        KLog.i("onViewAttachedToWindow")
        super.onViewAttachedToWindow(holder)
    }


    override fun onViewDetachedFromWindow(holder: K) {
        KLog.i("onViewDetachedFromWindow")
        super.onViewDetachedFromWindow(holder)
    }

}

abstract class BaseMultiItemAppAdapter<T : MultiItemEntity, K : BaseViewHolder>(data: List<T>?) : BaseMultiItemQuickAdapter<T, K>(data) {


    override fun bindToRecyclerView(recyclerView: RecyclerView?) {
        KLog.i("bindToRecyclerView ")
        super.bindToRecyclerView(recyclerView)
    }

}