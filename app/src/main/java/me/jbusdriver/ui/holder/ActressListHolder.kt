package me.jbusdriver.ui.holder

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_detail_actress.view.*
import me.jbusdriver.common.KLog
import me.jbusdriver.common.copy
import me.jbusdriver.common.inflate
import me.jbusdriver.common.toast
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.adapter.ActressInfoAdapter
import me.jbusdriver.ui.data.collect.ActressCollector

/**
 * Created by Administrator on 2017/5/9 0009.
 */
class ActressListHolder(context: Context) : BaseHolder(context) {
   private val actionMap by lazy {
        mapOf("复制名字" to { act: ActressInfo ->
            weakRef.get()?.let {
                it.copy(act.name)
                it.toast("已复制")
            }
        }, "收藏" to { act: ActressInfo ->
            ActressCollector.addToCollect(act)
            KLog.d("actress_data:${ActressCollector.dataList}")
        }, "取消收藏" to { act: ActressInfo ->
            ActressCollector.removeCollect(act)
            KLog.d("actress_data:${ActressCollector.dataList}")
        })
    }

    val view by lazy {
        weakRef.get()?.let {
            it.inflate(R.layout.layout_detail_actress).apply {
                rv_recycle_actress.layoutManager = LinearLayoutManager(it, LinearLayoutManager.HORIZONTAL, false)
                rv_recycle_actress.adapter = actressAdapter
                actressAdapter.setOnItemClickListener { _, _, position ->
                    actressAdapter.data.getOrNull(position)?.let {
                        item ->
                        KLog.d("item : $it")
                        weakRef.get()?.let {
                            MovieListActivity.start(it, item)
                        }
                    }
                }
                actressAdapter.setOnItemLongClickListener { _, view, position ->
                    actressAdapter.data.getOrNull(position)?.let {
                        act ->
                        val action = if (ActressCollector.has(act)) actionMap.minus("收藏")
                        else actionMap.minus("取消收藏")

                        MaterialDialog.Builder(view.context).title(act.name)
                                .items(action.keys)
                                .itemsCallback { _, _, _, text ->
                                    actionMap[text]?.invoke(act)
                                }
                                .show()
                    }
                    return@setOnItemLongClickListener true
                }
            }
        } ?: error("context ref is finish")
    }

    private val actressAdapter: BaseQuickAdapter<ActressInfo, BaseViewHolder> by lazy {
        ActressInfoAdapter(rxManager)
    }


    fun init(actress: List<ActressInfo>) {
        //actress
        if (actress.isEmpty()) view.tv_movie_actress_none_tip.visibility = View.VISIBLE
        else {
            //load header
            actressAdapter.setNewData(actress)
        }
    }

}