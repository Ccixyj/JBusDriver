package me.jbusdriver.db.service

import android.text.TextUtils
import io.reactivex.Observable
import me.jbusdriver.base.KLog
import me.jbusdriver.db.DB
import me.jbusdriver.db.bean.Category
import me.jbusdriver.db.bean.DBPage
import me.jbusdriver.db.bean.History
import me.jbusdriver.db.bean.LinkItem
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.bean.convertDBItem
import me.jbusdriver.ui.data.AppConfiguration
import java.util.*

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
        return DBPage(1, (count - 1) / pageSize + 1, pageSize)
    }

    fun queryPage(dbPage: DBPage): Observable<List<History>> =
            dao.queryByLimit(dbPage.pageSize, (dbPage.currentPage - 1) * dbPage.pageSize)

    fun clearAll() {
        dao.deleteAndSetZero()
    }
}

object CategoryService {
    private val dao by lazy { DB.categoryDao }
    private val snapShots = HashMap<Int, Category>()
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
                snapShots[it.toInt()] = category
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
    fun delete(category: Category, linkDBType: Int) {
        if (category.id in 1..10) error("cant delete category $category because id is in 1..10")
        snapShots.remove(category.id)
        dao.delete(category)
        LinkService.resetCategory(category, linkDBType)
    }

    /**
     * movie :1
     * actress : 2
     * ....
     * link : 10
     */
    fun queryCategoryTreeLike(type: Int) = dao.queryTreeByLike("$type/%").apply {
        this.forEach { v ->
            v.id?.let { snapShots.put(it, v) }
        }
    }


    fun getById(cId: Int): Category? {
        if (cId < 0) return null
        return snapShots.getOrPut(cId) {
            return dao.findById(cId)
        }
    }

    fun update(category: Category) {
        //移除可能的缓存
        snapShots.remove(category.id)
        require(category.id!! > 0)
        dao.update(category)
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
    fun remove(data: LinkItem) = dao.delete(data)
    fun queryMovies() = dao.listByType(1).let { it.mapNotNull { it.getLinkValue() as? Movie } }
    fun queryActress() = dao.listByType(2).let { it.mapNotNull { (it.getLinkValue() as? ActressInfo) } }

    fun queryLink() = dao.queryLink().let { it.map { it.getLinkValue() } }

    fun resetCategory(category: Category, dBType: Int) {
        KLog.d("reset $category")
        val pc = CategoryService.getById(category.pid)
        if (pc?.id != null) {
            category.id?.let {
                dao.updateByCategoryId(it, dBType, pc.id!!)
            }
        }

    }

    fun update(data: ILink) = dao.update(data.convertDBItem())
    fun update(data: LinkItem) = dao.update(data)
    fun queryByCategory(category: Category): List<LinkItem> {
        requireNotNull(category.id)
        return dao.queryByCategoryId(category.id!!)
    }

    fun queryAll() = dao.listAll()
    fun saveOrUpdate(backs: List<LinkItem>) {
        backs.forEach {
            val rowId = dao.insert(it) ?: -1
            if (rowId < 0){
                dao.update(it)
            }
        }
    }

    fun saveOrUpdate(back: LinkItem) {
        val rowId = dao.insert(back) ?: -1
        if (rowId < 0){
            dao.update(back)
        }
    }


    fun hasByKey(data: LinkItem) = dao.hasByKey(data)
}