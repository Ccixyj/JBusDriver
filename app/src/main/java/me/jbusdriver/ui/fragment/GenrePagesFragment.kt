package me.jbusdriver.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.util.ArrayMap
import android.view.View
import me.jbusdriver.common.*
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.GenrePageContract
import me.jbusdriver.mvp.GenrePageContract.GenrePagePresenter
import me.jbusdriver.mvp.bean.Genre
import me.jbusdriver.mvp.presenter.GenrePagePresenterImpl
import me.jbusdriver.ui.data.enums.DataSourceType

/**
 * Created by Administrator on 2017/7/17 0017.
 */
class GenrePagesFragment : TabViewPagerFragment<GenrePagePresenter, GenrePageContract.GenrePageView>(), GenrePageContract.GenrePageView {

    override val titleValues: MutableList<String> = mutableListOf()

    override val fragmentValues: MutableList<List<Genre>> = mutableListOf()

    private val fragmentsBak = mutableListOf<Fragment>()


    override fun createPresenter() = GenrePagePresenterImpl(arguments?.getString(C.BundleKey.Key_1) ?: error("no url for GenrePagesFragment"))

    override val mTitles: List<String>
        get() = titleValues

    override val mFragments: List<Fragment>
        get() = fragmentsBak

    override fun initWidget(rootView: View) {
        //super.initWidget(rootView)
    }

    override fun <T> showContent(data: T?) {
        require(titleValues.size == fragmentValues.size)
        fragmentValues.mapTo(fragmentsBak){
            GenreListFragment.newInstance(it)
        }
        initForViewPager()
    }

    companion object {
        fun newInstance(url: String) = GenrePagesFragment().apply {
            arguments = Bundle().apply {
                putString(C.BundleKey.Key_1, url)
            }
        }

        fun newInstance(type: DataSourceType) = GenrePagesFragment().apply {
            val urls = CacheLoader.acache.getAsString(C.Cache.BUS_URLS)?.let { AppContext.gson.fromJson<ArrayMap<String, String>>(it) } ?: arrayMapof()
            val url = urls[type.key] ?: JAVBusService.defaultFastUrl + "/genre"
            arguments = Bundle().apply {
                putString(C.BundleKey.Key_1, url)
            }
        }
    }


}