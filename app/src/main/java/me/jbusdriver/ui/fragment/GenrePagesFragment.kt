package me.jbusdriver.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.util.ArrayMap
import android.view.View
import me.jbusdriver.base.GSON
import me.jbusdriver.base.arrayMapof
import me.jbusdriver.base.fromJson
import me.jbusdriver.base.common.C
import me.jbusdriver.base.CacheLoader
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.GenrePageContract
import me.jbusdriver.mvp.GenrePageContract.GenrePagePresenter
import me.jbusdriver.mvp.bean.Genre
import me.jbusdriver.mvp.presenter.GenrePagePresenterImpl
import me.jbusdriver.ui.data.enums.DataSourceType

/**
 * 类别分类
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
        //请求数据完成后再加载
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

        fun newInstance(type: DataSourceType) = GenrePagesFragment().apply {
            val urls = CacheLoader.acache.getAsString(C.Cache.BUS_URLS)?.let { GSON.fromJson<ArrayMap<String, String>>(it) }
                    ?: arrayMapof()
            val url = urls[type.key] ?: JAVBusService.defaultFastUrl + "/genre"
            arguments = Bundle().apply {
                putString(C.BundleKey.Key_1, url)
            }
        }
    }


}