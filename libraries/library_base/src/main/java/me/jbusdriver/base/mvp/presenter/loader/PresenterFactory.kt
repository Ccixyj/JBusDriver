package me.jbusdriver.base.mvp.presenter.loader

/**
 * Created by Administrator on 2016/7/25 0025.
 */
interface PresenterFactory<out T> {
    fun createPresenter(): T
}
