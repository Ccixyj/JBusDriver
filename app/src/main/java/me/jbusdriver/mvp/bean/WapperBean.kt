package me.jbusdriver.mvp.bean

import com.chad.library.adapter.base.entity.AbstractExpandableItem
import me.jbusdriver.common.KLog
import me.jbusdriver.db.bean.Category

/**
 * Created by Administrator on 2017/9/26 0026.
 */

class ActressWrapper(private val categoryDec: Category? = null, val actressInfo: ActressInfo? = null) : AbstractExpandableItem<ActressWrapper>() {


    val category by lazy { categoryDec ?: subItems?.firstOrNull()?.actressInfo?.category }

    override fun getLevel(): Int {
        return if (actressInfo == null) {
            //菜单
            (category?.depth ?: 0).let {
                if (it in (0..1)) 0 else it
            }
        } else {
            -1
        }
    }

}