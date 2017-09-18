package me.jbusdriver.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import me.jbusdriver.db.HISTORYTable.COLUMN_CREATE_TIME
import me.jbusdriver.db.HISTORYTable.COLUMN_DES
import me.jbusdriver.db.HISTORYTable.COLUMN_ID
import me.jbusdriver.db.HISTORYTable.COLUMN_IMG
import me.jbusdriver.db.HISTORYTable.COLUMN_TYPE
import me.jbusdriver.db.HISTORYTable.COLUMN_URL
import me.jbusdriver.db.HISTORYTable.TABLE_NAME

private const val DB_NAME = "jbusdriver.db"
private const val DB_VERSION = 1
private const val CREATE_SQL = "CREATE TABLE $TABLE_NAME ( " +
        "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
        "$COLUMN_DES NVARCHAR(512) NOT NULL ," +
        "$COLUMN_URL VARCHAR(128)  NOT NULL ," +
        "$COLUMN_TYPE TINYINT  NOT NULL ," +
        "$COLUMN_IMG VARCHAR(128) ," +
        "$COLUMN_CREATE_TIME INTEGER  DEFAULT 0" +
        ")"

object HISTORYTable {
    const val TABLE_NAME = "t_history"
    const val COLUMN_ID = "id"
    const val COLUMN_DES = "des"
    const val COLUMN_URL = "url"
    const val COLUMN_TYPE = "DBtype"
    const val COLUMN_IMG = "image"
    const val COLUMN_CREATE_TIME = "createTime"
}


/**
 * Created by Administrator on 2017/9/18 0018.
 */
class JBusDBOpenHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_SQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }


}

