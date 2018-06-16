package me.jbusdriver.base.mvp

import android.content.Context
import io.reactivex.Flowable

/**
 * Created by Administrator on 2016/7/21 0021.
 */
interface BaseView {

    val viewContext: Context

    fun showLoading()

    fun dismissLoading()

    fun <T> showContent(data: T?): Unit = TODO(" no impl")

    fun showError(e: Throwable?): Unit = TODO(" no impl")





    interface BaseListView : BaseView {
        fun showContents(data: List<*>)
        //加载更多完成
        fun loadMoreComplete()

        //加载更多完毕
        fun loadMoreEnd(clickable: Boolean = false)

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

        //禁止加载更多
        fun enableLoadMore(bool: Boolean)

    }


}
