package me.jbusdriver.ui.fragment

import android.support.v4.app.Fragment
import me.jbusdriver.common.TabViewPagerFragment
import me.jbusdriver.mvp.MineCollectContract
import me.jbusdriver.mvp.presenter.MineCollectPresenterImpl
import me.jbusdriver.ui.data.DataSourceType

/**
 * Created by Administrator on 2017/7/17 0017.
 */
class MineCollectFragment :TabViewPagerFragment<MineCollectContract.MineCollectPresenter,MineCollectContract.MineCollectView>() , MineCollectContract.MineCollectView {
    override fun createPresenter() = MineCollectPresenterImpl()

    override val mTitles: List<String> by lazy { listOf("movies","girls") }

    override val mFragments: List<Fragment> by lazy {  listOf(HomeMovieListFragment.newInstance(DataSourceType.CENSORED),HomeMovieListFragment.newInstance(DataSourceType.CENSORED))  }

    companion object {
        fun  newInstance() =  MineCollectFragment()
    }
}