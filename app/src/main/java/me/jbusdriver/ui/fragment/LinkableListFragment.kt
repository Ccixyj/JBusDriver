package me.jbusdriver.ui.fragment

import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.common.AppBaseRecycleFragment
import me.jbusdriver.common.C
import me.jbusdriver.common.KLog
import me.jbusdriver.common.toast
import me.jbusdriver.mvp.LinkListContract
import me.jbusdriver.ui.activity.SearchResultActivity
import me.jbusdriver.ui.data.enums.DataSourceType

abstract class LinkableListFragment<T> : AppBaseRecycleFragment<LinkListContract.LinkListPresenter, LinkListContract.LinkListView, T>(), LinkListContract.LinkListView {

    override val layoutId: Int = R.layout.layout_swipe_recycle

    override val swipeView: SwipeRefreshLayout?  by lazy { sr_refresh }
    override val recycleView: RecyclerView by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager  by lazy { LinearLayoutManager(viewContext) }


    override fun onRestartInstance(bundle: Bundle) {
        super.onRestartInstance(bundle)
        arguments?.putBoolean(MENU_SHOW_ALL, bundle.getBoolean(MENU_SHOW_ALL, false))
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.main, menu)
        menu?.getItem(0)?.let {
            val mSearchView = MenuItemCompat.getActionView(it) as SearchView

            mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    KLog.i("search >> $query")
                    if (TextUtils.isEmpty(query)) viewContext.toast("关键字不能为空!")
                    gotoSearchResult(query)

                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu) //menu before show
        menu?.findItem(R.id.action_show_all)?.isChecked = arguments?.getBoolean(MENU_SHOW_ALL, false) ?: false
    }

    protected open fun gotoSearchResult(query: String) {
        SearchResultActivity.start(viewContext, query)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the CENSORED/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        when (id) {
            R.id.action_show_all -> {
                item.isChecked = !item.isChecked
                if (item.isChecked) item.title = "已发布" else item.title = "全部电影"  /*false : 已发布的 ,true :全部*/
                mBasePresenter?.loadAll(item.isChecked)
                arguments?.putBoolean(MENU_SHOW_ALL, item.isChecked)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(MENU_SHOW_ALL, arguments?.getBoolean(MENU_SHOW_ALL, false) ?: false)
    }


    /*================================================*/
    override val type by lazy { arguments?.getSerializable(C.BundleKey.Key_1) as? DataSourceType ?: DataSourceType.CENSORED }


    companion object {
        const val MENU_SHOW_ALL = "action_show_all"
    }
}