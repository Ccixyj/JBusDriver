package me.jbusdriver.ui.holder

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.chad.library.adapter.base.BaseViewHolder
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_collect_dir_edit.view.*
import me.jbusdriver.base.KLog
import me.jbusdriver.base.inflate
import me.jbusdriver.base.toast
import me.jbusdriver.db.bean.AllFirstParentDBCategoryGroup
import me.jbusdriver.db.bean.Category
import me.jbusdriver.ui.adapter.BaseAppAdapter

/**
 * Created by Administrator on 2017/11/2 0002.
 */


class CollectDirEditHolder(context: Context, parentCategory: Category) : BaseHolder(context) {

    private val delActionsParams = mutableSetOf<Category>()
    private val addActionsParams = mutableSetOf<Category>()
    private val collectDirs by lazy { categoryAdapter.data }


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
                    val txt = tv_add_category_name.text.toString().trim()
                    val add = if (txt.isNotBlank()) {
                        if (collectDirs.any { it.name == txt }) {
                            context.toast("$txt 分类已存在")
                            false
                        } else true

                    } else {
                        context.toast("请输入收藏夹名称")
                        false
                    }

                    if (add) {
                        val category = Category(txt, parentCategory.id
                                ?: -1, "${parentCategory.id}/")
                        addActionsParams.add(category)
                        categoryAdapter.addData(category)
                        categoryAdapter.notifyItemChanged(categoryAdapter.data.size - 1)
                        tv_add_category_name.setText("")
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
                    categoryAdapter.bindToRecyclerView(this)
                    categoryAdapter.setOnItemChildClickListener { _, view, position ->
                        KLog.d("$position view $view ")
                        when (view.id) {
                            R.id.tv_category_delete -> {
                                //删除
                                categoryAdapter.data.getOrNull(position)?.let {
                                    //具体删除逻辑
                                    if (it.id in (1..10)) return@setOnItemChildClickListener
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
        object : BaseAppAdapter<Category, BaseViewHolder>(R.layout.layout_collect_dir_edit_item) {
            private val exclude = AllFirstParentDBCategoryGroup.mapNotNull { it.value.id }
            override fun convert(holder: BaseViewHolder, item: Category) {
                holder.setText(R.id.tv_category_name, item.name)
                        .setVisible(R.id.tv_category_delete, item.id !in exclude)
                        .addOnClickListener(R.id.tv_category_delete)
            }
        }
    }

    /**
     * @param callback : 确认回调; first : 删除的元素 ,second : 添加的元素
     *
     */
    fun showDialogWithData(data: Collection<Category>, callback: (Set<Category>, Set<Category>) -> Unit) {
        //清空Action的参数
        delActionsParams.clear()
        addActionsParams.clear()
        //清空原有的数据
        categoryAdapter.data.clear()
        categoryAdapter.addData(data)

        MaterialDialog.Builder(view.context).customView(view, true)
                .dismissListener {
                    callback.invoke(delActionsParams, addActionsParams)
                }
                .show()

    }
}