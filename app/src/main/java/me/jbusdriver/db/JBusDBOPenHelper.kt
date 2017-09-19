package me.jbusdriver.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import me.jbusdriver.db.HISTORYTable.COLUMN_CREATE_TIME
import me.jbusdriver.db.HISTORYTable.COLUMN_DB_TYPE
import me.jbusdriver.db.HISTORYTable.COLUMN_ID
import me.jbusdriver.db.HISTORYTable.COLUMN_IS_ALL
import me.jbusdriver.db.HISTORYTable.COLUMN_JSON_STR
import me.jbusdriver.db.HISTORYTable.TABLE_NAME

private const val DB_NAME = "jbusdriver.db"
private const val DB_VERSION = 1
private const val CREATE_SQL = "CREATE TABLE $TABLE_NAME ( " +
        " $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
        " $COLUMN_DB_TYPE TINYINT  NOT NULL ," +
        " $COLUMN_CREATE_TIME INTEGER  DEFAULT 0 ," +
        " $COLUMN_JSON_STR TEXT  NOT NULL ," +
        " $COLUMN_IS_ALL TINYINT  NOT NULL " +
        ")"

object HISTORYTable {
    const val TABLE_NAME = "t_history"
    const val COLUMN_ID = "id"
    const val COLUMN_DB_TYPE = "dbType"
    const val COLUMN_CREATE_TIME = "createTime"
    const val COLUMN_JSON_STR = "jsonStr"
    const val COLUMN_IS_ALL = "isAll"
}


/**
 * data class History(val type: Int, val createTime: Date, val jsonStr: String)
 */
class JBusDBOpenHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_SQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }


}

