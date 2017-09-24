package me.jbusdriver.db.dao

import android.content.ContentValues
import com.squareup.sqlbrite2.BriteDatabase
import me.jbusdriver.db.LinkItemTable
import me.jbusdriver.db.bean.LinkItem

/**
 * Created by Administrator on 2017/9/18 0018.
 */
class LinkItemDao(private val db: BriteDatabase) {


    fun insert(link: LinkItem) = db.insert(LinkItemTable.TABLE_NAME, link.cv(true))

    fun delete(link: LinkItem) {
        db.delete(LinkItemTable.TABLE_NAME, "${LinkItemTable.COLUMN_ID} = ? ", link.id.toString())
    }


    companion object {
        fun LinkItem.cv(isInsert: Boolean): ContentValues = ContentValues().also {
            if (isInsert) it.put(LinkItemTable.COLUMN_CREATE_TIME, createTime.time)
            it.put(LinkItemTable.COLUMN_DB_TYPE, type)
            it.put(LinkItemTable.COLUMN_JSON_STR, jsonStr)
        }
    }


}