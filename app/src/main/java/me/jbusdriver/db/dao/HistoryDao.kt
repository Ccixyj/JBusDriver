package me.jbusdriver.db.dao

import android.content.ContentValues
import com.squareup.sqlbrite2.BriteDatabase
import io.reactivex.Observable
import me.jbusdriver.common.getIntByColumn
import me.jbusdriver.common.getLongByColumn
import me.jbusdriver.common.getStringByColumn
import me.jbusdriver.db.HISTORYTable
import me.jbusdriver.db.bean.History
import java.util.*


/**
 * Created by Administrator on 2017/9/18 0018.
 */
class HistoryDao(private val db: BriteDatabase) {


    fun insert(history: History) = db.insert(HISTORYTable.TABLE_NAME, history.cv(true))

    fun update(histories: List<History>) {
        val newTransaction = db.newTransaction()
        try {
            histories.forEach {
                db.update(HISTORYTable.TABLE_NAME, it.cv(false), HISTORYTable.COLUMN_ID + " = ? ",
                        it.id.toString())
            }
            newTransaction.markSuccessful()
        } finally {
            newTransaction.end()
        }
    }

    fun queryByLimit(size: Int, offset: Int): Observable<List<History>> {
        return db.createQuery(HISTORYTable.TABLE_NAME, "SELECT * FROM ${HISTORYTable.TABLE_NAME} ORDER BY ${HISTORYTable.COLUMN_ID} DESC LIMIT $offset , $size ").mapToList {
            History(it.getIntByColumn(HISTORYTable.COLUMN_DB_TYPE), Date(it.getLongByColumn(HISTORYTable.COLUMN_CREATE_TIME)),
                    it.getStringByColumn(HISTORYTable.COLUMN_JSON_STR) ?: "",
                    it.getIntByColumn(HISTORYTable.COLUMN_IS_ALL) == 1
            ).apply {
                id = it.getIntByColumn(HISTORYTable.COLUMN_ID)
            }
        }.flatMap { Observable.just(it) }
    }

    val count: Int = db.query("select count(1) from ${HISTORYTable.TABLE_NAME}").let {
        if (it.moveToFirst()) {
            it.getInt(0)
        } else -1
    }


    companion object {

        fun History.cv(isInsert: Boolean): ContentValues = ContentValues().also {
            if (isInsert) it.put(HISTORYTable.COLUMN_CREATE_TIME, createTime.time)
            it.put(HISTORYTable.COLUMN_DB_TYPE, type)
            it.put(HISTORYTable.COLUMN_JSON_STR, jsonStr)
            it.put(HISTORYTable.COLUMN_IS_ALL, if (isAll) 1 else 0)
        }

    }

    fun deleteAndSetZero() {
        db.run {
            delete(HISTORYTable.TABLE_NAME,null)
            execute("update sqlite_sequence SET seq = 0 where name = ?", arrayOf(HISTORYTable.TABLE_NAME))
        }
    }
}