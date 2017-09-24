package me.jbusdriver.db.dao

import android.content.ContentValues
import com.squareup.sqlbrite2.BriteDatabase
import io.reactivex.Observable
import me.jbusdriver.common.getIntByColumn
import me.jbusdriver.common.getLongByColumn
import me.jbusdriver.common.getStringByColumn
import me.jbusdriver.db.HistoryTable
import me.jbusdriver.db.bean.History
import java.util.*


/**
 * Created by Administrator on 2017/9/18 0018.
 */
class HistoryDao(private val db: BriteDatabase) {


    fun insert(history: History) = db.insert(HistoryTable.TABLE_NAME, history.cv(true))

    fun update(histories: List<History>) {
        val newTransaction = db.newTransaction()
        try {
            histories.forEach {
                db.update(HistoryTable.TABLE_NAME, it.cv(false), HistoryTable.COLUMN_ID + " = ? ",
                        it.id.toString())
            }
            newTransaction.markSuccessful()
        } finally {
            newTransaction.end()
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

    val count: Int = db.query("select count(1) from ${HistoryTable.TABLE_NAME}").let {
        if (it.moveToFirst()) {
            it.getInt(0)
        } else -1
    }

    fun deleteAndSetZero() {
        db.run {
            delete(HistoryTable.TABLE_NAME, null)
            execute("update sqlite_sequence SET seq = 0 where name = '${HistoryTable.TABLE_NAME}'")
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