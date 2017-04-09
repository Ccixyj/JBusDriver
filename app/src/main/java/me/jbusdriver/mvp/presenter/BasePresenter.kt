package me.jbusdriver.mvp.presenter

/**
 * Created by Administrator on 2016/7/21 0021.
 */
interface BasePresenter<V> {


    /**
     * Called when the view is attached to the presenter. Presenters should normally not use this
     * method since it's only used to link the view to the presenter which is done by the BasePresenter.
     * after activity's or fragment's onStart

     * @param view the view
     */
    fun onViewAttached(view: V)

    /**
     * Called every time the view starts, the view is guarantee to be not null starting at this
     * method, until [.onStop] is called.
     * after [.onViewAttached]

     * @param firstStart true if it's the first start of this presenter, only once in the presenter lifetime
     */
    fun onStart(firstStart: Boolean)

    /**
     * do not call manually, it will call if presenter is  firstStart
     */
    fun onFirstLoad()

    fun onResume()
    fun onPause()


    /**
     * Called every time the view stops. After this method, the view will be null until next
     * [.onStart] call.
     */
    fun onStop()

    /**
     * Called when the view is detached from the presenter. Presenters should normally not use this
     * method since it's only used to unlink the view from the presenter which is done by the BasePresenter.
     */
    fun onViewDetached()

    /**
     * Called when the presenter is definitely destroyed, you should use this method only to release
     * any resource used by the presenter (cancelCurrentRequest HTTP requests, close database connection...).
     */
    fun onPresenterDestroyed()


    //region 封装的接口


    /**
     * 加载更多的通用接口,presenter 继承 me.jbusdriver.mvp.presenter.AbstractRefreshLoadMorePresenterImpl，并实现view的loadComplete方法
     */
    interface LoadMorePresenter {
        /**
         * 加载数据

         * @param page 从1开始递增
         */
        fun loadData4Page(page: Int)

        /**
         * 处理加载更多
         */
        fun onLoadMore()

        /**
         * 是否需要加载下一页

         * @return
         */
        fun hasLoadNext(): Boolean

    }

    interface RefreshPresenter {
        /**
         * 下拉刷新开始时调用
         */
        fun onRefresh()
    }


    /***
     * 实现通用的列表presenter

     * @param
     */
    interface BaseRefreshLoadMorePresenter<V> : BasePresenter<V>, LoadMorePresenter, RefreshPresenter

    //viewpager + fragment 懒加载数据使用
    interface LazyLoaderPresenter {
        fun lazyLoad()
    }

}
