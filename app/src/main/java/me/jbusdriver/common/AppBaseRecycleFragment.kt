package me.jbusdriver.common

import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.reactivex.Flowable
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.mvp.BaseView
import me.jbusdriver.mvp.presenter.BasePresenter

/**
 * Created by Administrator on 2017/4/9.
 */
abstract class AppBaseRecycleFragment<P : BasePresenter.BaseRefreshLoadMorePresenter<V>, V : BaseView.BaseListWithRefreshView, M> : AppBaseFragment<P, V>(), BaseView.BaseListWithRefreshView {

    /**
     * view 销毁后获取时要从view中重新获取;
     * 重复使用fragment是不推荐lazy方式初始化.可能到时view引用的对象还是老对象.
     */
    abstract val swipeView: SwipeRefreshLayout?
    abstract val recycleView: RecyclerView
    abstract val layoutManager: RecyclerView.LayoutManager
    abstract val adapter: BaseQuickAdapter<M, in BaseViewHolder>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun initWidget(rootView: View) {
        recycleView.layoutManager = layoutManager
        adapter.setOnLoadMoreListener({ mBasePresenter?.onLoadMore() }, recycleView)
        adapter.openLoadAnimation {
            arrayOf(ObjectAnimator.ofFloat(it, "alpha", 0.0f, 1.0f),
                    ObjectAnimator.ofFloat(it, "translationY", 120f, 0f))
        }
        swipeView?.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimaryLight)
        swipeView?.setOnRefreshListener { mBasePresenter?.onRefresh() }
        recycleView.adapter = adapter
    }

    override fun showLoading() {
        KLog.t(TAG).d("showLoading")
        swipeView?.let {
            if (!it.isRefreshing) {
                it.post {
                    it.setProgressViewOffset(false, 0, viewContext.dpToPx(24f))
                    it.isRefreshing = true
                }
            }
        } ?: super.showLoading()

        adapter.removeAllFooterView()
    }

    override fun dismissLoading() {
        KLog.t(TAG).d("dismissLoading")
        swipeView?.let {
            it.post { it.isRefreshing = false }
        } ?: super.dismissLoading()
    }

    override fun showContents(data: List<*>) {
        KLog.d("showContents :$data")
        adapter.addData(data as MutableList<M>)
        //Diffutils
    }

    override fun loadMoreComplete() {
        adapter.loadMoreComplete()
    }

    override fun loadMoreEnd(clickable: Boolean) {
        adapter.loadMoreEnd()
        adapter.enableLoadMoreEndClick(clickable)
    }

    override fun loadMoreFail() {
        adapter.loadMoreFail()
    }

    override fun enableRefresh(bool: Boolean) {
        swipeView?.isEnabled = bool
    }

    override fun enableLoadMore(bool: Boolean) {
        adapter.setEnableLoadMore(bool)
    }

    override fun getRequestParams(page: Int): Flowable<String> = Flowable.empty()


    override fun resetList() {
        adapter.setNewData(null)
    }

    override fun showError(e: Throwable?) {

    }
}


