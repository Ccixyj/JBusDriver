package me.jbusdriver.db.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import com.squareup.sqlbrite2.BriteDatabase
import me.jbusdriver.common.KLog
import me.jbusdriver.common.getIntByColumn
import me.jbusdriver.common.getLongByColumn
import me.jbusdriver.common.getStringByColumn
import me.jbusdriver.db.LinkItemTable
import me.jbusdriver.db.bean.LinkItem
import java.util.*

/**
 * Created by Administrator on 2017/9/18 0018.
 */
class LinkItemDao(private val db: BriteDatabase) {


    fun insert(link: LinkItem): Long? {
        return try {
            KLog.i("LinkItemDao ${db.writableDatabase},$link")
            db.insert(LinkItemTable.TABLE_NAME, link.cv(true), CONFLICT_IGNORE)
        } catch (e: Exception) {
            null
        }
    }
    fun update(link: LinkItem): Boolean {
        return try {
            KLog.i("LinkItemDao ${db.writableDatabase},$link")
            db.update(LinkItemTable.TABLE_NAME, link.cv(false),LinkItemTable.COLUMN_KEY  + " = ? " ,link.key) > 0
        } catch (e: Exception) {
            false
        }
    }


    fun delete(link: LinkItem) = db.delete(LinkItemTable.TABLE_NAME, "${LinkItemTable.COLUMN_KEY} = ? ", link.key) > 0

    fun listAll(): List<LinkItem> = db.createQuery(LinkItemTable.TABLE_NAME, "SELECT * FROM ${LinkItemTable.TABLE_NAME} ORDER BY ${LinkItemTable.COLUMN_ID} DESC").mapToList {
        LinkItem(it.getIntByColumn(LinkItemTable.COLUMN_DB_TYPE), Date(it.getLongByColumn(LinkItemTable.COLUMN_CREATE_TIME)),
                it.getStringByColumn(LinkItemTable.COLUMN_KEY) ?: "", it.getStringByColumn(LinkItemTable.COLUMN_JSON_STR) ?: ""
        )
    }.blockingFirst()


    companion object {
        fun LinkItem.cv(isInsert: Boolean): ContentValues = ContentValues().also {
            if (isInsert) it.put(LinkItemTable.COLUMN_CREATE_TIME, createTime.time)
            it.put(LinkItemTable.COLUMN_DB_TYPE, type)
            it.put(LinkItemTable.COLUMN_CATEGORY_ID, categoryId)
            it.put(LinkItemTable.COLUMN_KEY, key)
            it.put(LinkItemTable.COLUMN_JSON_STR, jsonStr)
        }
    }

    fun listByType(i: Int): List<LinkItem> {
        return db.createQuery(LinkItemTable.TABLE_NAME, "SELECT * FROM ${LinkItemTable.TABLE_NAME} WHERE ${LinkItemTable.COLUMN_DB_TYPE} = ?  ORDER BY ${LinkItemTable.COLUMN_ID} DESC", i.toString()).mapToList {
            LinkItem(it.getIntByColumn(LinkItemTable.COLUMN_DB_TYPE), Date(it.getLongByColumn(LinkItemTable.COLUMN_CREATE_TIME)),
                    it.getStringByColumn(LinkItemTable.COLUMN_KEY) ?: "", it.getStringByColumn(LinkItemTable.COLUMN_JSON_STR) ?: "").apply {
                categoryId = it.getIntByColumn(LinkItemTable.COLUMN_CATEGORY_ID)
            }
        }.blockingFirst()
    }

    fun queryLink() = db.createQuery(LinkItemTable.TABLE_NAME, "SELECT * FROM ${LinkItemTable.TABLE_NAME} WHERE ${LinkItemTable.COLUMN_DB_TYPE} NOT IN (1,2)  ORDER BY ${LinkItemTable.COLUMN_ID} DESC").mapToList {
        LinkItem(it.getIntByColumn(LinkItemTable.COLUMN_DB_TYPE), Date(it.getLongByColumn(LinkItemTable.COLUMN_CREATE_TIME)),
                it.getStringByColumn(LinkItemTable.COLUMN_KEY) ?: "", it.getStringByColumn(LinkItemTable.COLUMN_JSON_STR) ?: ""
        )
    }.blockingFirst()

    fun updateByCategoryId(id: Int, type: Int) {
        val cv = ContentValues().apply { putNull(LinkItemTable.COLUMN_CATEGORY_ID) }
        db.update(LinkItemTable.TABLE_NAME, cv, " ${LinkItemTable.COLUMN_CATEGORY_ID} = ? and ${LinkItemTable.COLUMN_DB_TYPE} = ? ", id.toString(), type.toString())
    }

}