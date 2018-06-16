package me.jbusdriver.db.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import com.squareup.sqlbrite3.BriteDatabase
import com.squareup.sqlbrite3.inTransaction
import io.reactivex.Observable
import me.jbusdriver.base.getIntByColumn
import me.jbusdriver.base.getLongByColumn
import me.jbusdriver.base.getStringByColumn
import me.jbusdriver.db.HistoryTable
import me.jbusdriver.db.bean.History
import java.util.*


/**
 * Created by Administrator on 2017/9/18 0018.
 */
class HistoryDao(private val db: BriteDatabase) {


    fun insert(history: History) = try {
        ioBlock { db.insert(HistoryTable.TABLE_NAME, CONFLICT_IGNORE, history.cv(true)) }
    } catch (e: Exception) {
        -1
    }


    fun update(histories: List<History>) {
        db.inTransaction {
            histories.forEach {
                db.update(HistoryTable.TABLE_NAME, CONFLICT_IGNORE, it.cv(false), HistoryTable.COLUMN_ID + " = ? ",
                        it.id.toString())
            }
        }
    }

    fun queryByLimit(size: Int, offset: Int): Observable<List<History>> {
        return db.createQuery(HistoryTable.TABLE_NAME, "SELECT * FROM ${HistoryTable.TABLE_NAME} ORDER BY ${HistoryTable.COLUMN_ID} DESC LIMIT $offset , $size ").mapToList {
            History(it.getIntByColumn(HistoryTable.COLUMN_DB_TYPE), Date(it.getLongByColumn(HistoryTable.COLUMN_CREATE_TIME)),
                    it.getStringByColumn(HistoryTable.COLUMN_JSON_STR) ?: "",
                    it.getIntByColumn(HistoryTable.COLUMN_IS_ALL) == 1
            ).apply {
                id = it.getIntByColumn(HistoryTable.COLUMN_ID)
            }
        }.flatMap { Observable.just(it) }
    }

    val count: Int
        get() = db.query("select count(1) from ${HistoryTable.TABLE_NAME}").let {
            if (it.moveToFirst()) {
                it.getInt(0)
            } else -1
        }

    fun deleteAndSetZero() {
        TryIgnoreEx {
            ioBlock {
                db.run {
                    delete(HistoryTable.TABLE_NAME, null)
                    execute("update sqlite_sequence SET seq = 0 where name = '${HistoryTable.TABLE_NAME}'")
                }
            }
        }
    }


    companion object {

        fun History.cv(isInsert: Boolean): ContentValues = ContentValues().also {
            if (isInsert) it.put(HistoryTable.COLUMN_CREATE_TIME, createTime.time)
            it.put(HistoryTable.COLUMN_DB_TYPE, type)
            it.put(HistoryTable.COLUMN_JSON_STR, jsonStr)
            it.put(HistoryTable.COLUMN_IS_ALL, if (isAll) 1 else 0)
        }

    }
}


