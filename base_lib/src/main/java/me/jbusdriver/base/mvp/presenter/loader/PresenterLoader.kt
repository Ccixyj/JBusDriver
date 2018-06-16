package me.jbusdriver.base.mvp.presenter.loader

import android.content.Context
import android.support.v4.content.Loader
import me.jbusdriver.base.KLog
import me.jbusdriver.base.mvp.presenter.BasePresenter

/**
 * Created by Administrator on 2016/7/25 0025.\

 * 在手机状态改变时不会被销毁

 * 会在Activity/Fragment不再被使用后由系统回收。

 * 与Activity/Fragment的生命周期绑定，所以事件会自己分发。

 * 每一个Activity/Fragment持有自己的Loader对象的引用，所以可以同时存在多个Presenter-Activity/Fragment组合，比如说在ViewPager中。

 * 可以同步运行，自己确定什么时候数据准备好了可以被传递。
 */
class PresenterLoader<T : BasePresenter<*>>
/**
 * Stores away the application context associated with context.
 * Since Loaders can be used across multiple activities it's dangerous to
 * store the context directly; always use [.getContext] to retrieve
 * the Loader's Context, don't use the constructor argument directly.
 * The Context returned by [.getContext] is safe to use across
 * Activity instances.

 * @param context used to retrieve the application context.
 */
(context: Context, private val factory: PresenterFactory<T>) : Loader<T>(context) {

    private var presenter: T? = null


    override fun onStartLoading() {

        // 如果已经有Presenter实例那就直接返回
        if (presenter != null) {
            deliverResult(presenter)
            return
        }
        // 如果没有
        forceLoad()
    }

    override fun onForceLoad() {
        // 通过工厂来实例化Presenter
        presenter = factory.createPresenter()
        // 返回Presenter
        deliverResult(presenter)
    }


    override fun onReset() {
        KLog.d("PresenterLoader: current SPresenter $presenter destroy")
        presenter?.onPresenterDestroyed()
        presenter = null
    }
}
