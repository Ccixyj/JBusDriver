package me.jbusdriver.db.dao

import android.content.ContentValues
import com.squareup.sqlbrite2.BriteDatabase
import me.jbusdriver.db.HISTORYTable
import me.jbusdriver.db.bean.History


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

    fun quergroupByDate(){

    }


    companion object {

        fun History.cv(isInsert: Boolean): ContentValues = ContentValues().also {
            if (isInsert) it.put(HISTORYTable.COLUMN_CREATE_TIME, createTime.time)
            it.put(HISTORYTable.COLUMN_DES, des)
            it.put(HISTORYTable.COLUMN_URL, url)
            it.put(HISTORYTable.COLUMN_TYPE, type)
            it.put(HISTORYTable.COLUMN_IMG, img)
        }

    }
}