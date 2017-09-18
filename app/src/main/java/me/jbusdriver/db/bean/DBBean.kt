package me.jbusdriver.db.bean

import java.util.*

/**
 * Created by Administrator on 2017/9/18 0018.
 */
data class History(val des: String, val url: String, val type: Int, val createTime: Date,val img: String? = null) {
    val id: Int? = null
}