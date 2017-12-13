package me.jbusdriver.db.service

import android.text.TextUtils
import com.umeng.analytics.pro.db
import io.reactivex.Observable
import me.jbusdriver.common.KLog
import me.jbusdriver.db.CategoryTable
import me.jbusdriver.db.DB
import me.jbusdriver.db.bean.*
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.bean.convertDBItem
import me.jbusdriver.ui.data.AppConfiguration

/**
 * Created by Administrator on 2017/9/18 0018.
 */
object HistoryService {

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

object CategoryService {
    private val dao by lazy { DB.categoryDao }
    private val catCache = hashMapOf(
            1L to MovieCategory,
            2L to ActressCategory,
            3L to LinkCategory)
//    apply {
//        category = if (it.categoryId > 0) {
//            cats.getOrPut(it.categoryId) { categoryDao.findById(it.categoryId) ?: ActressCategory }
//        } else {
//            ActressCategory
//        }
//    }

    fun insert(category: Category): Category {
        return dao.insert(category).let {
            if (it != -1L) {
                catCache.put(it, category)
                category.id = it.toInt()
            } else {
                KLog.w("save $category error return id : $it")
            }
            category
        }
    }

    /**
     * 删除对应分类
     * 重置所有收藏
     */
    fun delete(category: Category, actressDBType: Int) {
        if (category.id in 1..3) error("cant delete category $category because id is in 1..3")
        catCache.remove(category.id?.toLong())
        dao.delete(category)
        LinkService.resetCategory(category, actressDBType)
    }

    /**
     * movie :1
     * actress : 2
     * link : 3
     */
    fun queryTreeByLike(type: Int) = dao.queryTreeByLike("/$type/%").apply {
        this.forEach { v ->
            v.id?.let { catCache.put(it.toLong(), v) }
        }
    }


    fun getById(cId: Int): Category {
        if (cId < 0) return error("Category id must > 0")
        return catCache.getOrPut(cId.toLong()) {
            return dao.findById(cId)
        }
    }

}

object LinkService {
    private val dao by lazy { DB.linkDao }

    fun save(data: ILink) = dao.insert(data.convertDBItem()) != null

    fun save(data: List<ILink>): Boolean {
        return data.mapNotNull {
            dao.insert(it.convertDBItem())
        }.size == data.size
    }

    fun remove(data: ILink) = dao.delete(data.convertDBItem())
    fun queryMovies() = dao.listByType(1).let { it.mapNotNull { it.getLinkValue() as? Movie } }
    fun queryActress() = dao.listByType(2).let { it.mapNotNull { (it.getLinkValue() as? ActressInfo) } }

    fun queryLink() = dao.queryLink().let { it.map { it.getLinkValue() } }

    fun resetCategory(category: Category, dBType: Int) {
        KLog.d("reset $category")
        category.id?.let {
            dao.updateByCategoryId(it, dBType)
        }

    }
}