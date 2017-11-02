package me.jbusdriver.ui.holder

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_collect_dir_edit.view.*
import me.jbusdriver.common.KLog
import me.jbusdriver.common.inflate
import me.jbusdriver.common.toast
import me.jbusdriver.db.bean.ActressCategory
import me.jbusdriver.db.bean.Category

/**
 * Created by Administrator on 2017/11/2 0002.
 */


class CollectDirEditHolder(context: Context) : BaseHolder(context) {

    val delActionsParams = mutableSetOf<Category>()
    val addActionsParams = mutableSetOf<Category>()


    val view by lazy {
        weakRef.get()?.let { context ->
            context.inflate(R.layout.layout_collect_dir_edit).apply {
                tv_category_add.setOnClickListener {
                    AnimatorSet().apply {
                        playTogether(
                                ObjectAnimator.ofFloat(ll_add_category, "alpha", 1.0f, 0.0f),
                                ObjectAnimator.ofFloat(ll_add_category_edit, "alpha", 0.0f, 1.0f),
                                ObjectAnimator.ofFloat(ll_add_category_edit, "translationY", 60f, 0f).apply {
                                    addListener(

                                            object : AnimatorListenerAdapter() {
                                                override fun onAnimationStart(animation: Animator?) {
                                                    ll_add_category_edit.visibility = View.VISIBLE
                                                }

                                                override fun onAnimationEnd(animation: Animator?) {
                                                    ll_add_category.visibility = View.GONE
                                                }
                                            }
                                    )

                                })
                        duration = 300

                    }.start()

                }

                tv_category_add_confirm.setOnClickListener {
                    val txt = tv_add_category_name.text.toString()
                    if (txt.isNotBlank()){
                       addActionsParams.add( Category(txt, ActressCategory.id ?: -1,"/${ActressCategory.id}/"))
                    }else{
                        context.toast("请输入收藏夹名称")
                    }

                    AnimatorSet().apply {
                        playTogether(
                                ObjectAnimator.ofFloat(ll_add_category, "alpha", 0.0f, 1.0f),
                                ObjectAnimator.ofFloat(ll_add_category_edit, "alpha", 1.0f, 0.0f),
                                ObjectAnimator.ofFloat(ll_add_category_edit, "translationY", 0f, -60f).apply {
                                    addListener(
                                            object : AnimatorListenerAdapter() {
                                                override fun onAnimationEnd(animation: Animator?) {
                                                    ll_add_category.visibility = View.VISIBLE
                                                    ll_add_category_edit.visibility = View.GONE
                                                }
                                            }
                                    )

                                }
                        )
                        duration = 300
                    }.start()
                }

                rv_category_list.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = categoryAdapter
                    categoryAdapter.setOnItemChildClickListener { adapter, view, position ->
                        KLog.d("$position view $view ")
                        when (view.id) {
                            R.id.tv_category_delete -> {
                                //删除
                                categoryAdapter.data.getOrNull(position)?.let {
                                    //具体删除逻辑
                                    categoryAdapter.data.removeAt(position)
                                    categoryAdapter.notifyItemRemoved(position)
                                    delActionsParams.add(it)
                                }

                            }
                            else -> Unit
                        }
                    }

                }
            }
        } ?: error("CollectDirEditHolder can not inflate view for context is null ")
    }

    private val categoryAdapter by lazy {
        object : BaseQuickAdapter<Category, BaseViewHolder>(R.layout.layout_collect_dir_edit_item) {
            override fun convert(holder: BaseViewHolder, item: Category) {
                holder.setText(R.id.tv_category_name, item.name)
                        .setVisible(R.id.tv_category_delete, item.depth != 0)
                        .addOnClickListener(R.id.tv_category_delete)
            }
        }
    }

    fun initData(datas: List<Category>) {
        //清空Action的参数
        delActionsParams.clear()
        //清空原有的数据
        categoryAdapter.data.clear()
        categoryAdapter.addData(datas)
    }
}