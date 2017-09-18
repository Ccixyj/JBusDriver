package me.jbusdriver.db.service

import me.jbusdriver.db.DB
import me.jbusdriver.db.bean.History
import me.jbusdriver.ui.data.AppConfiguration

/**
 * Created by Administrator on 2017/9/18 0018.
 */
class HistoryService {

    val dao by lazy { DB.historyDao }

    fun insert(history: History) {
        if (AppConfiguration.enableHistory) dao.insert(history)
    }
}