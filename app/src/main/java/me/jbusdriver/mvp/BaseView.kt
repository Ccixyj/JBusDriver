package com.cfzx.mvp.view

import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.Flowable
import me.jbusdriver.common.AppContext

/**
 * Created by Administrator on 2016/7/21 0021.
 */
interface BaseView {

    companion object {
        var place_holder_loading: MaterialDialog? = null
    }

    val viewContext: Context

    fun showLoading(): Unit {
        if (viewContext is AppContext) return
        place_holder_loading = MaterialDialog.Builder(viewContext).content("正在加载...").progress(true, 0).show()
    }

    fun dismissLoading(): Unit {
        place_holder_loading?.dismiss()
        place_holder_loading = null
    }

    fun <T> showContent(data: T?): Unit = TODO(" no impl")

    fun showError(e: Throwable?): Unit = TODO(" no impl")

    interface BaseListView : BaseView {
        fun showContents(datas: List<*>?)
        //加载更多完成
        fun loadMoreComplete()

        //加载更多完毕
        fun loadMoreEnd(clickable:Boolean = false)

        //加载更多失败
        fun loadMoreFail()

        fun enableRefresh(bool: Boolean)


    }

    interface BaseListWithRefreshView : BaseListView {

        /**
         * 请求参数
         * @param page
         * *
         * @return
         */
        fun getRequestParams(page: Int): Flowable<String>


        /**
         * 重置列表
         */
        fun resetList()

        fun enableLoadMore(bool: Boolean)

    }


}
