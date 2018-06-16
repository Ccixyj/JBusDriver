package me.jbusdriver.ui.fragment

import android.support.v4.app.Fragment
import me.jbusdriver.base.common.C
import me.jbusdriver.mvp.MineCollectContract
import me.jbusdriver.mvp.bean.SearchLink
import me.jbusdriver.mvp.presenter.MineCollectPresenterImpl
import me.jbusdriver.ui.data.enums.SearchType

/**
 * Created by Administrator on 2017/7/17 0017.
 */
class SearchResultPagesFragment : TabViewPagerFragment<MineCollectContract.MineCollectPresenter, MineCollectContract.MineCollectView>(), MineCollectContract.MineCollectView {

    private val searchWord by lazy { arguments?.getString(C.BundleKey.Key_1) ?: error("must set search word") }

    override fun createPresenter() = MineCollectPresenterImpl()

    override val mTitles: List<String> by lazy { SearchType.values().map { it.title } }

    override val mFragments: List<Fragment> by lazy {
        SearchType.values().map {
            if (it == SearchType.ACTRESS) {
                ActressListFragment.newInstance(SearchLink(it, searchWord))
            } else {
                LinkedMovieListFragment.newInstance(SearchLink(it, searchWord))
            }
        }
    }


}