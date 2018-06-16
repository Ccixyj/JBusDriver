package me.jbusdriver.db.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import com.squareup.sqlbrite3.BriteDatabase
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import me.jbusdriver.base.KLog
import me.jbusdriver.base.getIntByColumn
import me.jbusdriver.base.getLongByColumn
import me.jbusdriver.base.getStringByColumn
import me.jbusdriver.db.LinkItemTable
import me.jbusdriver.db.bean.LinkItem
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Administrator on 2017/9/18 0018.
 */
class LinkItemDao(private val db: BriteDatabase) {


    fun insert(link: LinkItem): Long? {
        return try {
            KLog.i("LinkItemDao ${db.writableDatabase},$link")
            ioBlock { db.insert(LinkItemTable.TABLE_NAME, CONFLICT_IGNORE, link.cv(true)) }
        } catch (e: Exception) {
            null
        }
    }

    fun update(link: LinkItem): Boolean {
        return try {
            KLog.i("LinkItemDao ${db.writableDatabase},$link")
            ioBlock { db.update(LinkItemTable.TABLE_NAME, CONFLICT_IGNORE, link.cv(false), LinkItemTable.COLUMN_KEY + " = ? ", link.key) > 0 }
        } catch (e: Exception) {
            false
        }
    }


    fun delete(link: LinkItem) = try {
        ioBlock { db.delete(LinkItemTable.TABLE_NAME, "${LinkItemTable.COLUMN_DB_TYPE} = ? AND ${LinkItemTable.COLUMN_KEY} = ? ", link.type.toString(), link.key) > 0 }
    } catch (e: Exception) {
        false
    }

    fun listAll(): Flowable<List<LinkItem>> = db.createQuery(LinkItemTable.TABLE_NAME, "SELECT * FROM ${LinkItemTable.TABLE_NAME} ORDER BY ${LinkItemTable.COLUMN_ID} DESC").mapToList {
        LinkItem(it.getIntByColumn(LinkItemTable.COLUMN_DB_TYPE), Date(it.getLongByColumn(LinkItemTable.COLUMN_CREATE_TIME)),
                it.getStringByColumn(LinkItemTable.COLUMN_KEY)
                        ?: "", it.getStringByColumn(LinkItemTable.COLUMN_JSON_STR) ?: "",
                it.getIntByColumn(LinkItemTable.COLUMN_CATEGORY_ID)
        ).apply {
            id = it.getIntByColumn(LinkItemTable.COLUMN_ID)
        }
    }.timeout(6, TimeUnit.SECONDS).take(1).toFlowable(BackpressureStrategy.DROP)


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
                    it.getStringByColumn(LinkItemTable.COLUMN_KEY)
                            ?: "", it.getStringByColumn(LinkItemTable.COLUMN_JSON_STR)
                    ?: "").apply {
                categoryId = it.getIntByColumn(LinkItemTable.COLUMN_CATEGORY_ID)
            }
        }.blockingFirst(emptyList()) ?: emptyList()
    }

    fun queryLink() = db.createQuery(LinkItemTable.TABLE_NAME, "SELECT * FROM ${LinkItemTable.TABLE_NAME} WHERE ${LinkItemTable.COLUMN_DB_TYPE} NOT IN (1,2)  ORDER BY ${LinkItemTable.COLUMN_ID} DESC").mapToList {
        LinkItem(it.getIntByColumn(LinkItemTable.COLUMN_DB_TYPE), Date(it.getLongByColumn(LinkItemTable.COLUMN_CREATE_TIME)),
                it.getStringByColumn(LinkItemTable.COLUMN_KEY)
                        ?: "", it.getStringByColumn(LinkItemTable.COLUMN_JSON_STR) ?: "",
                it.getIntByColumn(LinkItemTable.COLUMN_CATEGORY_ID)
        )
    }.timeout(6, TimeUnit.SECONDS).blockingFirst()

    fun queryByCategoryId(id: Int): List<LinkItem> = db.createQuery(LinkItemTable.TABLE_NAME, "SELECT * FROM ${LinkItemTable.TABLE_NAME} WHERE ${LinkItemTable.COLUMN_CATEGORY_ID} = ?  ORDER BY ${LinkItemTable.COLUMN_ID} DESC", id).mapToList {
        LinkItem(it.getIntByColumn(LinkItemTable.COLUMN_DB_TYPE), Date(it.getLongByColumn(LinkItemTable.COLUMN_CREATE_TIME)),
                it.getStringByColumn(LinkItemTable.COLUMN_KEY)
                        ?: "", it.getStringByColumn(LinkItemTable.COLUMN_JSON_STR) ?: "",
                it.getIntByColumn(LinkItemTable.COLUMN_CATEGORY_ID)
        )
    }.timeout(6, TimeUnit.SECONDS).blockingFirst(emptyList()) ?: emptyList()

    fun updateByCategoryId(id: Int, type: Int, setId: Int) {
        KLog.d("updateByCategoryId $id $type -> set $setId")
        val cv = ContentValues().apply { put(LinkItemTable.COLUMN_CATEGORY_ID, setId) }
        val res = db.update(LinkItemTable.TABLE_NAME, CONFLICT_IGNORE, cv, " ${LinkItemTable.COLUMN_CATEGORY_ID} = ? and ${LinkItemTable.COLUMN_DB_TYPE} = ? ", id.toString(), type.toString())
        KLog.d("updateByCategoryId affected $res")
    }

    fun hasByKey(item: LinkItem): Int {
        return db.query("SELECT count(1) FROM ${LinkItemTable.TABLE_NAME} WHERE ${LinkItemTable.COLUMN_DB_TYPE} = ? AND ${LinkItemTable.COLUMN_KEY} = ?",
                item.type, item.key).let {
            if (it.moveToFirst()) {
                it.getInt(0)
            } else -1
        }
    }

}