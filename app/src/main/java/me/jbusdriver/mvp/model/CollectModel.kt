package me.jbusdriver.mvp.model

import com.afollestad.materialdialogs.MaterialDialog
import me.jbusdriver.base.JBusManager
import me.jbusdriver.base.toast
import me.jbusdriver.db.bean.LinkItem
import me.jbusdriver.db.service.CategoryService
import me.jbusdriver.db.service.LinkService
import me.jbusdriver.mvp.bean.ActressDBType
import me.jbusdriver.mvp.bean.MovieDBType
import me.jbusdriver.ui.data.AppConfiguration

/**
 * Created by Administrator on 2018/2/13.
 */
object CollectModel {


    fun getCollectType(data: LinkItem): Int {
        return when {
            data.type == MovieDBType -> MovieDBType
            data.type == ActressDBType -> ActressDBType
            else -> 10
        }
    }

    /**
     * @return 现在无法使用返回值判断是否收藏成功
     */
    fun addToCollect(data: LinkItem): Boolean {
        LinkService.saveOrUpdate(listOf(data))
        toast("收藏成功")

        return true
    }

    fun has(data: LinkItem) = LinkService.hasByKey(data) >= 1

    fun removeCollect(data: LinkItem) = LinkService.remove(data).also {
        toast("已经取消收藏")
    }


    fun update(data: LinkItem) = LinkService.update(data)

    fun addToCollectForCategory(data: LinkItem, callBack: Boolean.() -> Unit = {}) {
        if (AppConfiguration.enableCategory) {
            val cs = CategoryService.queryCategoryTreeLike(getCollectType(data))
            if (cs.size > 1) {
                JBusManager.manager.lastOrNull()?.get()?.let {
                    MaterialDialog.Builder(it).title("选择添加的分类")
                        .items(cs.map { it.name })
                        .itemsCallbackSingleChoice(0) { _, _, i, _ ->
                            data.categoryId = cs.getOrNull(i)?.id ?: -1
                            callBack.invoke(addToCollect(data))
                            return@itemsCallbackSingleChoice true
                        }
                        .positiveText("添加")
                        .show()
                    return
                }
            }
        }
        callBack.invoke(addToCollect(data))

    }
}