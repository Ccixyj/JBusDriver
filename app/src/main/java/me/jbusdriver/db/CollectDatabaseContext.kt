package me.jbusdriver.db

import android.content.Context
import android.content.ContextWrapper
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import me.jbusdriver.common.KLog
import java.io.File
import java.io.IOException

/**
 * 用于支持对存储在SD卡上的数据库的访问
 */
abstract class SDCardDatabaseContext
/**
 * 构造函数
 *
 * @param base 上下文环境
 */
(base: Context) : ContextWrapper(base) {


    /*根目录下目录，数据库所在目录*/
    abstract val dir: String

    /**
     * 获得数据库路径，如果不存在，则创建对象对象
     * @param name
     */
    override fun getDatabasePath(name: String): File? {

        //判断是否存在sd卡
        val sdExist = android.os.Environment.MEDIA_MOUNTED == android.os.Environment.getExternalStorageState()
        if (!sdExist) {//如果不存在,
            KLog.e("SD卡管理：", "SD卡不存在，请加载SD卡")
            return null
        } else {//如果存在
            //获取sd卡路径
            val dbDir = android.os.Environment.getExternalStorageDirectory().toString() + File.separator + dir + File.separator
            val dbPath = dbDir + name//数据库路径
            //判断目录是否存在，不存在则创建该目录
            val dirFile = File(dbDir)
            if (!dirFile.exists())
                dirFile.mkdirs()

            //数据库文件是否创建成功
            var isFileCreateSuccess = false
            //判断文件是否存在，不存在则创建该文件
            val dbFile = File(dbPath)
            if (!dbFile.exists()) {
                try {
                    isFileCreateSuccess = dbFile.createNewFile()//创建文件
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            } else
                isFileCreateSuccess = true

            //返回数据库文件对象
            return if (isFileCreateSuccess)
                dbFile
            else
                null
        }
    }

    override fun openOrCreateDatabase(name: String, mode: Int, factory: CursorFactory?) = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null)

    override fun openOrCreateDatabase(name: String, mode: Int, factory: CursorFactory?,
                                      errorHandler: DatabaseErrorHandler?) = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null)
}