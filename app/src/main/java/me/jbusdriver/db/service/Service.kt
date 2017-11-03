package me.jbusdriver.db.service

import android.text.TextUtils
import io.reactivex.Observable
import me.jbusdriver.common.KLog
import me.jbusdriver.db.DB
import me.jbusdriver.db.bean.ActressCategory
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
    private val linkService by lazy { LinkService() }

    fun insert(category: Category) {
        dao.insert(category)
    }

    /**
     * 删除对应分类
     * 重置所有收藏
     */
    fun delete(category: Category, actressDBType: Int) {
        dao.delete(category)
        linkService.resetCategory(category, actressDBType)
    }

    /**
     * movie :1
     * actress : 2
     * link : 3
     */
    fun queryTreeByLike(type: Int)  =  dao.queryTreeByLike("/$type/%")

}

class LinkService {
    private val dao by lazy { DB.linkDao }
    private val categoryDao by lazy { DB.categoryDao }

    fun save(data: ILink) = dao.insert(data.convertDBItem()) != null

    fun save(datas: List<ILink>): Boolean {
        return datas.mapNotNull {
            dao.insert(it.convertDBItem())
        }.size == datas.size
    }

    fun remove(data: ILink) = dao.delete(data.convertDBItem())
    fun queryMovies() = dao.listByType(1).let { it.mapNotNull { it.getLinkValue() as? Movie } }
    fun queryActress() = dao.listByType(2).let {
        val cats = hashMapOf<Int, Category>()
        it.mapNotNull {
            (it.getLinkValue() as? ActressInfo)?.apply {
                category = if (it.categoryId > 0) {
                    cats.getOrPut(it.categoryId) { categoryDao.findById(it.categoryId) ?: ActressCategory }
                } else {
                    ActressCategory
                }
            }
        }
    }

    fun queryLink() = dao.queryLink().let { it.map { it.getLinkValue() } }

    fun resetCategory(category: Category, dBType: Int) {
        KLog.d("reset $category")
        category.id?.let {
            dao.updateByCategoryId(it, dBType)
        }

    }
}