package me.jbusdriver.db.dao

import android.content.ContentValues
import com.squareup.sqlbrite2.BriteDatabase
import me.jbusdriver.db.CategoryTable
import me.jbusdriver.db.bean.Category

/**
 * Created by Administrator on 2017/9/18 0018.
 */
class CategoryDao(private val db: BriteDatabase) {


    fun insert(category: Category) = db.insert(CategoryTable.TABLE_NAME, category.cv())

    fun delete(category: Category) {
        db.delete(CategoryTable.TABLE_NAME, "${CategoryTable.COLUMN_ID} = ? ", category.id.toString())
    }


    companion object {
        fun Category.cv(): ContentValues = ContentValues().also {
            it.put(CategoryTable.COLUMN_NAME, name)
        }
    }


}