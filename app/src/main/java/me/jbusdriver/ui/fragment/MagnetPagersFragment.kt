package me.jbusdriver.ui.fragment

import android.support.v4.app.Fragment
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.base.common.C
import me.jbusdriver.mvp.MagnetPagerContract
import me.jbusdriver.mvp.presenter.MagnetPagerPresenterImpl
import me.jbusdriver.ui.data.AppConfiguration
import me.jbusdriver.ui.data.magnet.MagnetLoaders

/**
 * Created by Administrator on 2017/7/17 0017.
 */
class MagnetPagersFragment : TabViewPagerFragment<MagnetPagerContract.MagnetPagerPresenter, MagnetPagerContract.MagnetPagerView>(), MagnetPagerContract.MagnetPagerView {

    private val keyword by lazy {
        arguments?.getString(C.BundleKey.Key_1) ?: error("must set keyword")
    }


    override fun createPresenter() = MagnetPagerPresenterImpl()

    override val mTitles: List<String> by lazy {
        val allKeys = MagnetLoaders.keys
        AppConfiguration.MagnetKeys.filter { allKeys.contains(it) }.apply {
            Schedulers.single().scheduleDirect {
                AppConfiguration.MagnetKeys.clear()
                AppConfiguration.MagnetKeys.addAll(this)
                AppConfiguration.saveMagnetKeys()
            }
        }


    }

    override val mFragments: List<Fragment> by lazy {
        mTitles.map { MagnetListFragment.newInstance(keyword, it) }
    }


}