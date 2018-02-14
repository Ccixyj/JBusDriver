package me.jbusdriver.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Menu
import android.view.MenuInflater
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.mvp.MineCollectContract
import me.jbusdriver.mvp.presenter.MineCollectPresenterImpl
import me.jbusdriver.ui.data.AppConfiguration

/**
 * since 1.1 remove info menu
 */
class MineCollectFragment : TabViewPagerFragment<MineCollectContract.MineCollectPresenter, MineCollectContract.MineCollectView>(), MineCollectContract.MineCollectView {
    override fun createPresenter() = MineCollectPresenterImpl()

    override val mTitles: List<String> by lazy { listOf("电影", "演员", "链接") }

    override val mFragments: List<Fragment> by lazy { listOf(MovieCollectFragment.newInstance(), ActressCollectFragment.newInstance(), LinkCollectFragment.newInstance()) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (AppConfiguration.enableCategory) inflater?.inflate(R.menu.menu_collect, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        menu?.findItem(R.id.action_collect_dir_edit)?.isVisible = AppConfiguration.enableCategory
    }


    companion object {
        fun newInstance() = MineCollectFragment()
    }
}