package me.jbusdriver.db

import com.squareup.sqlbrite2.SqlBrite
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.common.AppContext
import me.jbusdriver.common.KLog
import me.jbusdriver.db.dao.CategoryDao
import me.jbusdriver.db.dao.HistoryDao
import me.jbusdriver.db.dao.LinkItemDao
import java.io.File


/**
 * Created by Administrator on 2017/9/18 0018.
 */

object DB {

    private val provideSqlBrite: SqlBrite by lazy {
        val builder = SqlBrite.Builder()
        if (jbusdriver.me.jbusdriver.BuildConfig.DEBUG) {
            builder.logger { message -> KLog.i(message) }
        }
        builder.build()
    }

    private val dataBase by lazy {
        provideSqlBrite.wrapDatabaseHelper(JBusDBOpenHelper(AppContext.instace), Schedulers.io()).apply {
            setLoggingEnabled(jbusdriver.me.jbusdriver.BuildConfig.DEBUG)
        }
    }
    private val collectDataBase by lazy {
        provideSqlBrite.wrapDatabaseHelper(CollectDBOpenHelper(
                object : SDCardDatabaseContext(AppContext.instace) {
                    override val dir: String = AppContext.instace.packageName + File.separator + "collect"
                }), Schedulers.io()).apply {
            setLoggingEnabled(jbusdriver.me.jbusdriver.BuildConfig.DEBUG)
        }
    }


    val historyDao by lazy { HistoryDao(dataBase) }
    val categoryDao by lazy { CategoryDao(collectDataBase) }
    val linkDao by lazy { LinkItemDao(collectDataBase) }
}