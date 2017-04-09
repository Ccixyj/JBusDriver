package me.jbusdriver.mvp.presenter

import com.cfzx.mvp.view.BaseView
import me.jbusdriver.common.KLog
import me.jbusdriver.mvp.bean.Page
import me.jbusdriver.mvp.bean.hasNext

/**
 * Created by Administrator on 2016/9/6 0006.
 * 通用下拉加在更多 , 上拉刷新处理 处理,可单独使用其中一部分
 */
abstract class AbstractRefreshLoadMorePresenterImpl<V : BaseView.BaseListWithRefreshView> : BasePresenterImpl<V>(), BasePresenter.BaseRefreshLoadMorePresenter<V> {

    protected var page = Page()
    override fun onFirstLoad() {
        loadData4Page(1)//首次加载 可以从内存中读取
    }


    override fun onLoadMore() {
        KLog.d("onLoadMore :" + hasLoadNext())
        if (hasLoadNext()) {
            loadData4Page(page.nextPage.page)
        }
    }

    override fun hasLoadNext(): Boolean = page.hasNext

    override fun onRefresh() = loadData4Page(1)


}
