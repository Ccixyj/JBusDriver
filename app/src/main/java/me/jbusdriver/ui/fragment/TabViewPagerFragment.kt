package me.jbusdriver.ui.fragment

import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.view.View
import me.jbusdriver.base.mvp.BaseView
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_mine_collect.*
import me.jbusdriver.base.common.AppBaseFragment
import me.jbusdriver.base.mvp.presenter.BasePresenter

/**
 * Created by Administrator on 2017/7/17 0017.
 */
abstract class TabViewPagerFragment<P : BasePresenter<V>, V : BaseView> : AppBaseFragment<P, V>() {

    abstract val mTitles: List<String>
    abstract val mFragments: List<Fragment>

    override val layoutId = R.layout.layout_mine_collect

    override fun initWidget(rootView: View) {
        initForViewPager()
    }

    protected fun initForViewPager() {
        mTitles.forEach { tabLayout.addTab(tabLayout.newTab().setText(it)) }
        vp_fragment.offscreenPageLimit = mTitles.size
        vp_fragment.adapter = pagerAdapter
        tabLayout.setupWithViewPager(vp_fragment)
        tabLayout.setTabsFromPagerAdapter(pagerAdapter)
        require(mTitles.size == mFragments.size)
        if (mTitles.size >= 5) {
            tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        }
    }

    private val pagerAdapter: FragmentPagerAdapter by lazy {
        require(mTitles.size == mFragments.size)
        object : FragmentPagerAdapter(childFragmentManager) {


            override fun getItem(position: Int): Fragment {
                if (mFragments.size >= position) {
                    return mFragments[position]
                } else {
                    error("you must put fragment in mFragments and size equal mTitles")
                }

            }

            override fun getCount(): Int = mTitles.size

            override fun getPageTitle(position: Int): CharSequence = mTitles[position]
        }
    }
}