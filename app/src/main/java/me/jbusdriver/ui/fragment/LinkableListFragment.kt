package me.jbusdriver.ui.fragment

import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.text.InputType
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_recycle.*
import kotlinx.android.synthetic.main.layout_seek_page.view.*
import kotlinx.android.synthetic.main.layout_swipe_recycle.*
import me.jbusdriver.common.*
import me.jbusdriver.mvp.LinkListContract
import me.jbusdriver.mvp.bean.PageChangeEvent
import me.jbusdriver.mvp.bean.PageInfo
import me.jbusdriver.ui.activity.SearchResultActivity
import me.jbusdriver.ui.data.AppConfiguration

abstract class LinkableListFragment<T> : AppBaseRecycleFragment<LinkListContract.LinkListPresenter, LinkListContract.LinkListView, T>(), LinkListContract.LinkListView {

    override val layoutId: Int = R.layout.layout_swipe_recycle

    override val swipeView: SwipeRefreshLayout? by lazy { sr_refresh }
    override val recycleView: RecyclerView  by lazy { rv_recycle }
    override val layoutManager: RecyclerView.LayoutManager  by lazy { LinearLayoutManager(viewContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindRx()
    }

    private fun bindRx() {
        RxBus.toFlowable(PageChangeEvent::class.java)
                .subscribeBy(onNext = {
                    KLog.d("PageChangeEvent $it")
                    activity?.invalidateOptionsMenu() //刷新菜单
                    mBasePresenter?.onRefresh()
                }).addTo(rxManager)
    }


    override fun restoreState(bundle: Bundle) {
        super.restoreState(bundle)
        val all = bundle.getBoolean(MENU_SHOW_ALL, false)
        tempSaveBundle.putBoolean(MENU_SHOW_ALL, all)
        if (all) {
            mBasePresenter?.setAll(true)
        }
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

                override fun onQueryTextChange(newText: String) = false
            })
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu) //menu before show
        KLog.d("onPrepareOptionsMenu ${AppConfiguration.pageMode}")
        menu?.findItem(R.id.action_show_all)?.isChecked = tempSaveBundle.getBoolean(MENU_SHOW_ALL, false)
        menu?.findItem(R.id.action_jump)?.let {
            it.isVisible = AppConfiguration.pageMode == AppConfiguration.PageMode.Page
        }
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
                mBasePresenter?.setAll(item.isChecked)
                mBasePresenter?.loadData4Page(1)
                tempSaveBundle.putBoolean(MENU_SHOW_ALL, item.isChecked)
            }
            R.id.action_jump -> {
                mBasePresenter?.currentPageInfo?.let {
                    showPageDialog(it)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(MENU_SHOW_ALL, tempSaveBundle.getBoolean(MENU_SHOW_ALL, false))
    }


    /*================================================*/
//    override val type by lazy {
//        arguments?.getSerializable(C.BundleKey.Key_1) as? DataSourceType ?: DataSourceType.CENSORED
//    }

    protected fun showPageDialog(info: PageInfo) {
        if (info.referPages.isEmpty()) return
        if (info.referPages.size == 1 && info.referPages.first() == 1) {
            viewContext.toast("当前共一页")
            return
        }
        val seekView = viewContext.inflate(R.layout.layout_seek_page)
        seekView.bsb_seek_page?.apply {

            try {
                val max = this.javaClass.getDeclaredField("mMax")
                max?.isAccessible = true
                max?.setFloat(this, info.referPages.last().toFloat())

//                this.javaClass.declaredMethods.forEach { KLog.d(it) }
                this.javaClass.getDeclaredMethod("initConfigByPriority").also {
                    it.isAccessible = true
                    it.invoke(this)
                }

//                this.javaClass.getDeclaredMethod("calculateRadiusOfBubble").also {
//                    it.isAccessible = true
//                    it.invoke(this)
//                }

                setProgress(info.activePage.toFloat())
                this.post {
                    this.invalidate()
                    this.requestLayout()
                }
            } catch (e: Exception) {
                KLog.e("error :$e")
            }
        }
        MaterialDialog.Builder(viewContext).customView(seekView, false)
                .neutralText("输入页码").onNeutral { dialog, _ ->
                    showEditDialog(info)
                    dialog.dismiss()
                }.positiveText("跳转").onPositive { _, _ ->
                    seekView.bsb_seek_page?.progress?.let {
                        mBasePresenter?.jumpToPage(it)
                        adapter.notifyLoadMoreToLoading()
                    }
                }.show()
//        MaterialDialog.Builder(viewContext).title("跳转:").items(info.pages.map {
//            "${if (it > info.activePage) " 👇 跳至" else if (it == info.activePage) " 👉 当前" else " 👆 跳至"} 第 $it 页"
//        }).itemsCallback { _, _, position, _ ->
//            info.pages.getOrNull(position)?.let {
//                mBasePresenter?.jumpToPage(it)
//                adapter.notifyLoadMoreToLoading()
//            }
//        }.neutralText("输入页码").onNeutral { dialog, _ ->
//            showEditDialog(info)
//            dialog.dismiss()
//        }.show()
    }

    private fun showEditDialog(info: PageInfo) {
        MaterialDialog.Builder(viewContext).title("输入页码:")
                .input("输入跳转页码", null, false, { dialog, input ->
                    KLog.d("page $input")
                    input.toString().toIntOrNull()?.let {
                        if (it < 1) {
                            viewContext.toast("必须输入大于0的整数!")
                            return@input
                        }
                        mBasePresenter?.jumpToPage(it)
                        dialog.dismiss()
                    } ?: let {
                        viewContext.toast("必须输入数字!")
                    }
                })
                .autoDismiss(false)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .neutralText("选择页码").onNeutral { dialog, _ ->
                    showPageDialog(info)
                    dialog.dismiss()
                }.show()
    }


    override val pageMode: Int
        get() = AppConfiguration.pageMode

    companion object {
        const val MENU_SHOW_ALL = "action_show_all"
    }
}