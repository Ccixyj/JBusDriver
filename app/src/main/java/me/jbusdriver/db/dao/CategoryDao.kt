package me.jbusdriver.db.dao

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.squareup.sqlbrite3.BriteDatabase
import me.jbusdriver.common.getIntByColumn
import me.jbusdriver.common.getStringByColumn
import me.jbusdriver.db.CategoryTable
import me.jbusdriver.db.bean.Category
import java.util.concurrent.TimeUnit

/**
 * Created by Administrator on 2017/9/18 0018.
 */
class CategoryDao(private val db: BriteDatabase) {


    fun insert(category: Category) = try {
        ioBlock {
            val id = db.insert(CategoryTable.TABLE_NAME, SQLiteDatabase.CONFLICT_IGNORE, category.cv())
            require(id > 0)
            update(category.copy(tree = category.tree + "id"))
            id
        }
    } catch (e: Exception) {
        -1L
    }

    fun delete(category: Category) {
        ioBlock { db.delete(CategoryTable.TABLE_NAME, "${CategoryTable.COLUMN_ID} = ? ", category.id.toString()) }
    }

    fun findById(cId: Int): Category {
        return db.createQuery(CategoryTable.TABLE_NAME, "select * from ${CategoryTable.TABLE_NAME}  where ${CategoryTable.COLUMN_ID} = ?", cId.toString())
                .mapToOne { toCategory(it) }.timeout(3, TimeUnit.SECONDS).blockingFirst()
    }

    private fun toCategory(it: Cursor): Category {
        return Category(it.getStringByColumn(CategoryTable.COLUMN_NAME)
                ?: "", it.getIntByColumn(CategoryTable.COLUMN_P_ID),
                it.getStringByColumn(CategoryTable.COLUMN_TREE) ?: ""
        ).apply {
            id = it.getIntByColumn(CategoryTable.COLUMN_ID)
        }
    }

    fun queryTreeByLike(like: String): List<Category> {
        return db.createQuery(CategoryTable.TABLE_NAME, "select * from ${CategoryTable.TABLE_NAME}  where ${CategoryTable.COLUMN_TREE} like ? ORDER BY ${CategoryTable.COLUMN_ORDER} DESC", like)
                .mapToList { toCategory(it) }.timeout(6, TimeUnit.SECONDS).blockingFirst()
    }

    fun update(category: Category): Boolean {
        return try {
            ioBlock { db.update(CategoryTable.TABLE_NAME, SQLiteDatabase.CONFLICT_IGNORE, category.cv(), CategoryTable.COLUMN_ID + " = ${category.id!!} ") > 0 }
        } catch (e: Exception) {
            false
        }

    }
}