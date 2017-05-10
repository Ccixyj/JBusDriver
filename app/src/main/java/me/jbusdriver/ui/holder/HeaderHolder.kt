package me.jbusdriver.ui.holder

import android.content.Context
import android.graphics.Paint
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_detail_header.view.*
import me.jbusdriver.common.KLog
import me.jbusdriver.common.inflate
import me.jbusdriver.mvp.bean.Header
import me.jbusdriver.ui.activity.MovieListActivity
import me.jbusdriver.ui.data.DataSourceType

/**
 * Created by Administrator on 2017/5/9 0009.
 */
class HeaderHolder(context: Context, type: DataSourceType) {

    val view by lazy {
        context.inflate(R.layout.layout_detail_header, null).apply {
            rv_recycle_header.layoutManager = LinearLayoutManager(this.context)
            rv_recycle_header.adapter = headAdapter
        }
    }

    val headAdapter = object : BaseQuickAdapter<Header, BaseViewHolder>(R.layout.layout_header_item) {
        override fun convert(helper: BaseViewHolder, item: Header) {
            helper.getView<TextView>(R.id.tv_head_value)?.apply {
                if (!TextUtils.isEmpty(item.link)) {
                    setTextColor(ResourcesCompat.getColor(this@apply.resources, R.color.colorPrimaryDark, null))
                    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                    setOnClickListener {
                        KLog.d("text : $item")
                        MovieListActivity.start(context, type, item)
                    }
                } else {
                    setTextColor(ResourcesCompat.getColor(this@apply.resources, R.color.secondText, null))
                    paintFlags = 0
                    setOnClickListener(null)
                }
            }
            helper.setText(R.id.tv_head_name, item.name)
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