package me.jbusdriver.db.dao

import android.database.Cursor
import com.squareup.sqlbrite2.BriteDatabase
import me.jbusdriver.common.getIntByColumn
import me.jbusdriver.common.getStringByColumn
import me.jbusdriver.db.CategoryTable
import me.jbusdriver.db.bean.Category

/**
 * Created by Administrator on 2017/9/18 0018.
 */
class CategoryDao(private val db: BriteDatabase) {


    fun insert(category: Category) = db.insert(CategoryTable.TABLE_NAME, category.cv())

    fun delete(category: Category) {
        if (category.id in 1..3) error("cant delete category $category because id is in 1..3")
        db.delete(CategoryTable.TABLE_NAME, "${CategoryTable.COLUMN_ID} = ? ", category.id.toString())
    }

    fun findById(cId: Int): Category? {
        return db.createQuery(CategoryTable.TABLE_NAME, "select * from ${CategoryTable.TABLE_NAME}  where ${CategoryTable.COLUMN_ID} = ?", cId.toString())
                .mapToOne {  toCategory(it) }.blockingFirst()
    }

    private fun toCategory(it: Cursor): Category {
        return Category(it.getStringByColumn(CategoryTable.COLUMN_NAME) ?: "", it.getIntByColumn(CategoryTable.COLUMN_P_ID),
                it.getStringByColumn(CategoryTable.COLUMN_TREE) ?: ""
        ).apply {
            id = it.getIntByColumn(CategoryTable.COLUMN_ID)
        }
    }

    fun queryTreeByLike(like: String) : List<Category>{
       return db.createQuery(CategoryTable.TABLE_NAME,"select * from ${CategoryTable.TABLE_NAME}  where ${CategoryTable.COLUMN_TREE} like ? ",like)
                .mapToList { toCategory(it) }.blockingFirst()
    }
}