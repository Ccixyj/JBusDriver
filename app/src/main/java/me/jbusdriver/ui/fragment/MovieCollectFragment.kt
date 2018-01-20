package me.jbusdriver.ui.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import com.afollestad.materialdialogs.MaterialDialog
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.common.KLog
import me.jbusdriver.common.toast
import me.jbusdriver.mvp.LinkListContract
import me.jbusdriver.mvp.presenter.MovieCollectPresenterImpl
import me.jbusdriver.ui.data.AppConfiguration
import me.jbusdriver.ui.data.collect.MovieCollector
import me.jbusdriver.ui.holder.CollectDirEditHolder

/**
 * Created by Administrator on 2017/7/17 0017.
 */
class MovieCollectFragment : AbsMovieListFragment(), LinkListContract.LinkListView {

    private val holder by lazy {
        CollectDirEditHolder(viewContext)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.findItem(R.id.action_collect_dir_edit)?.setOnMenuItemClickListener {
//            holder.showDialogWithData(actGroupMap.keys.toList()) { delActionsParams, addActionsParams ->
//                KLog.d("$delActionsParams $addActionsParams")
//                if (delActionsParams.isNotEmpty()) {
//                    delActionsParams.forEach {
//                        try {
//                            categoryService.delete(it, ActressDBType)
//                        } catch (e: Exception) {
//                            viewContext.toast("不能删除默认分类")
//                        }
//                    }
//                }
//
//                if (addActionsParams.isNotEmpty()) {
//                    addActionsParams.forEach {
//                        categoryService.insert(it)
//                    }
//                }
//                mBasePresenter?.onRefresh()
//            }
            true
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter.setOnItemChildLongClickListener { adapter, view, position ->
            KLog.d("setOnItemChildLongClickListener $position")
            this@MovieCollectFragment.adapter.getData().getOrNull(position)?.let {
                MaterialDialog.Builder(viewContext)
                        .title(it.title)
                        .items(listOf("取消收藏"))
                        .itemsCallback { _, _, _, text ->
                            if (MovieCollector.removeCollect(it)) {
                                viewContext.toast("取消收藏成功")
                                adapter.data.removeAt(position)
                                adapter.notifyItemRemoved(position)
                            } else {
                                viewContext.toast("已经取消了")
                            }
                        }
                        .show()
            }

            return@setOnItemChildLongClickListener true
        }

    }

    override val pageMode: Int = AppConfiguration.PageMode.Normal
    override fun createPresenter() = MovieCollectPresenterImpl(MovieCollector)

    companion object {
        fun newInstance() = MovieCollectFragment()
    }
}