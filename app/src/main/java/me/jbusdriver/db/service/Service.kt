package me.jbusdriver.db.service

import android.text.TextUtils
import io.reactivex.Observable
import me.jbusdriver.db.DB
import me.jbusdriver.db.bean.DBPage
import me.jbusdriver.db.bean.History
import me.jbusdriver.ui.data.AppConfiguration

/**
 * Created by Administrator on 2017/9/18 0018.
 */
class HistoryService {

    private val dao by lazy { DB.historyDao }

    fun insert(history: History) {
        if (AppConfiguration.enableHistory && !TextUtils.isEmpty(history.jsonStr)) dao.insert(history)
    }

    fun page(pageSize: Int = 20): DBPage {
        val count = dao.count
        return DBPage(1, (count - 1) / 20 + 1, pageSize)
    }

    fun queryPage(dbPage: DBPage): Observable<List<History>> =
            dao.queryByLimit(dbPage.pageSize, (dbPage.currentPage - 1) * dbPage.pageSize)
    fun clearAll(){
        dao.deleteAndSetZero()
    }
}