package me.jbusdriver.ui.fragment

import android.os.Bundle
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.LinkListContract
import me.jbusdriver.mvp.bean.PageLink
import me.jbusdriver.mvp.presenter.HomeMovieListPresenterImpl
import me.jbusdriver.ui.data.enums.DataSourceType


/**
 * Created by Administraor on 2017/4/9.
 */
class HomeMovieListFragment : AbsMovieListFragment(), LinkListContract.LinkListView {
    override fun createPresenter() = HomeMovieListPresenterImpl(type, PageLink(1, "", JAVBusService.defaultFastUrl) /*PageLink没什么用,默认设置JAVBusService.defaultFastUrl就可以*/)

    /*================================================*/
    companion object {
        fun newInstance(type: DataSourceType) = HomeMovieListFragment().apply {
            arguments = Bundle().apply { putSerializable(MOVIE_LIST_DATA_TYPE, type) }
        }
    }

}