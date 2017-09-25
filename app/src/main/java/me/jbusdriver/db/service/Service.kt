package me.jbusdriver.db.service

import android.text.TextUtils
import io.reactivex.Observable
import me.jbusdriver.db.DB
import me.jbusdriver.db.bean.Category
import me.jbusdriver.db.bean.DBPage
import me.jbusdriver.db.bean.History
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.bean.convertDBItem
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

    fun clearAll() {
        dao.deleteAndSetZero()
    }
}

class CategoryService {
    private val dao by lazy { DB.categoryDao }
    fun insert(history: Category) {
        dao.insert(history)
    }

}

class LinkService {
    private val dao by lazy { DB.linkDao }

    fun save(data: ILink) = dao.insert(data.convertDBItem()) != null

    fun save(datas: List<ILink>): Boolean {
        return datas.mapNotNull {
            dao.insert(it.convertDBItem())
        }.size == datas.size
    }

    fun remove(data: ILink) = dao.delete(data.convertDBItem())
    fun queryMovies() = dao.listByType(1).let { it.mapNotNull { it.getLinkValue() as? Movie } }
    fun queryActress() = dao.listByType(2).let { it.mapNotNull { it.getLinkValue() as? ActressInfo } }
    fun queryLink() = dao.queryLink().let { it.map { it.getLinkValue() } }
}