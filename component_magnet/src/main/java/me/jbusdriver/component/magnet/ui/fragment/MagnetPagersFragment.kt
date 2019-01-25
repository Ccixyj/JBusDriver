package me.jbusdriver.component.magnet.ui.fragment

import android.support.v4.app.Fragment
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.base.common.C
import me.jbusdriver.base.ui.fragment.TabViewPagerFragment
import me.jbusdriver.component.magnet.MagnetPluginHelper.MagnetLoaders
import me.jbusdriver.component.magnet.mvp.MagnetPagerContract.MagnetPagerPresenter
import me.jbusdriver.component.magnet.mvp.MagnetPagerContract.MagnetPagerView
import me.jbusdriver.component.magnet.mvp.presenter.MagnetPagerPresenterImpl
import me.jbusdriver.component.magnet.ui.config.Configuration

/**
 * Created by Administrator on 2017/7/17 0017.
 */
class MagnetPagersFragment : TabViewPagerFragment<MagnetPagerPresenter, MagnetPagerView>(), MagnetPagerView {

    private val keyword by lazy {
        arguments?.getString(C.BundleKey.Key_1) ?: error("must set keyword")
    }


    override fun createPresenter() = MagnetPagerPresenterImpl()

    override val mTitles: List<String> by lazy {
        val allKeys = MagnetLoaders.keys
        Configuration.getConfigKeys().filter { allKeys.contains(it) }.apply {
            Schedulers.single().scheduleDirect {
                Configuration.saveMagnetKeys(this)
            }
        }


    }

    override val mFragments: List<Fragment> by lazy {
        mTitles.map { MagnetListFragment.newInstance(keyword, it) }
    }


}