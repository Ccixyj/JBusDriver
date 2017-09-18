package me.jbusdriver.ui.fragment

import android.os.Bundle
import me.jbusdriver.common.C
import me.jbusdriver.common.toast
import me.jbusdriver.db.DB
import me.jbusdriver.mvp.LinkListContract
import me.jbusdriver.mvp.bean.PageLink
import me.jbusdriver.mvp.presenter.HomeMovieListPresenterImpl
import me.jbusdriver.ui.data.DataSourceType


/**
 * Created by Administraor on 2017/4/9.
 */
class HomeMovieListFragment : AbsMovieListFragment(), LinkListContract.LinkListView {
    override fun createPresenter() = HomeMovieListPresenterImpl(type, PageLink(1, "", "") /*没什么用*/)

    /*================================================*/
    companion object {
        fun newInstance(type: DataSourceType) = HomeMovieListFragment().apply {
            arguments = Bundle().apply { putSerializable(C.BundleKey.Key_1, type) }
            this.viewContext.toast( DB.historyDao.count.toString() )
        }
    }

}