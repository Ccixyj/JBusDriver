package me.jbusdriver.ui.holder

import android.content.Context
import android.graphics.Paint
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_detail_header.view.*
import me.jbusdriver.common.KLog
import me.jbusdriver.common.copy
import me.jbusdriver.common.inflate
import me.jbusdriver.common.toast
import me.jbusdriver.mvp.bean.Header
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.data.collect.LinkCollector

/**
 * Created by Administrator on 2017/5/9 0009.
 */
class HeaderHolder(context: Context) : BaseHolder(context) {

    val actionMap by lazy {
        mapOf("复制" to { header: Header ->
            weakRef.get()?.let {
                it.copy(header.value)
                it.toast("已复制")

            }
        }, "收藏" to { header ->
            LinkCollector.addToCollect(header)
            KLog.d("link data ${LinkCollector.dataList}")
        }, "取消收藏" to { header ->
            LinkCollector.removeCollect(header)
            KLog.d("link data ${LinkCollector.dataList}")
        })
    }

    val view by lazy {
        weakRef.get()?.let {
            it.inflate(R.layout.layout_detail_header, null).apply {
                rv_recycle_header.layoutManager = LinearLayoutManager(this.context)
                rv_recycle_header.adapter = headAdapter
                rv_recycle_header.isNestedScrollingEnabled = true
            }
        } ?: error("context ref is finish")
    }

    private val headAdapter = object : BaseQuickAdapter<Header, BaseViewHolder>(R.layout.layout_header_item) {
        override fun convert(holder: BaseViewHolder, item: Header) {
            holder.getView<TextView>(R.id.tv_head_value)?.apply {
                if (!TextUtils.isEmpty(item.link)) {
                    setTextColor(ResourcesCompat.getColor(this@apply.resources, R.color.colorPrimaryDark, null))
                    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG

                    setOnClickListener {
                        KLog.d("setOnClickListener text : $item")
                        MovieListActivity.start(it.context, item)
                    }

                } else {
                    setTextColor(ResourcesCompat.getColor(this@apply.resources, R.color.secondText, null))
                    paintFlags = 0
                    setOnClickListener(null)
                }
                //长按操作
                setOnLongClickListener {
                    KLog.d("setOnLongClickListener text : $item")
                    val action =   actionMap.filter {
                        when {
                            TextUtils.isEmpty(item.link) -> it.key == "复制"
                            LinkCollector.has(item) -> it.key != "收藏"
                            else -> it.key != "取消收藏"
                        }
                    }


                    MaterialDialog.Builder(holder.itemView.context).title(item.name).content(item.value)
                            .items(action.keys)
                            .itemsCallback { _, _, _, text ->
                                action[text]?.invoke(item)
                            }.show()
                    return@setOnLongClickListener true

                }
            }
            holder.setText(R.id.tv_head_name, item.name)
                    .setText(R.id.tv_head_value, item.value)
        }
    }

    fun init(data: List<Header>) {
        //header
        if (data.isEmpty()) view.tv_movie_head_none_tip.visibility = View.VISIBLE
        else {
            //load header
            headAdapter.setNewData(data)
        }
    }

}