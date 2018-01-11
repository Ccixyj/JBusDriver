package me.jbusdriver.ui.fragment

import android.support.v4.app.Fragment
import me.jbusdriver.common.C
import me.jbusdriver.mvp.MagnetPagerContract
import me.jbusdriver.mvp.presenter.MagnetPagerPresenterImpl
import me.jbusdriver.ui.data.AppConfiguration

/**
 * Created by Administrator on 2017/7/17 0017.
 */
class MagnetPagersFragment : TabViewPagerFragment<MagnetPagerContract.MagnetPagerPresenter, MagnetPagerContract.MagnetPagerView>(), MagnetPagerContract.MagnetPagerView {

    private val keyword by lazy { arguments.getString(C.BundleKey.Key_1) ?: error("must set keyword") }


    override fun createPresenter() = MagnetPagerPresenterImpl()

    override val mTitles: List<String> by lazy { AppConfiguration.MagnetKeys }

    override val mFragments: List<Fragment> by lazy {
        mTitles.map { MagnetListFragment.newInstance(keyword, it) }
    }


}