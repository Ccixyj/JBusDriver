package me.jbusdriver.common.bean.db

import android.content.ContentValues
import me.jbusdriver.base.arrayMapof

data class Category(val name: String, val pid: Int = -1, val tree: String, var order: Int = 0) {
    var id: Int? = null

    @delegate:Transient
    val depth: Int by lazy { tree.split("/").filter { it.isNotBlank() }.size }

    fun cv(update: Boolean = false): ContentValues = ContentValues().also {
        if (id != null || update) it.put(CategoryTable.COLUMN_ID, id)
        it.put(CategoryTable.COLUMN_NAME, name)
        it.put(CategoryTable.COLUMN_P_ID, pid)
        it.put(CategoryTable.COLUMN_TREE, tree)
        it.put(CategoryTable.COLUMN_ORDER, order)
    }

    override fun equals(other: Any?) =
            other?.let { (it as? Category)?.id == this.id } ?: false

    fun equalAll(other: Category?) = other?.let { it.id == this.id && it.name == this.name && it.pid == this.pid && it.tree == this.tree }
            ?: false
}

/**
 * 预留 [3..9]的分类
 */
val MovieCategory = Category("默认电影分类", -1, "1/", Int.MAX_VALUE).apply { id = 1 }
val ActressCategory = Category("默认演员分类", -1, "2/", Int.MAX_VALUE).apply { id = 2 }
val LinkCategory = Category("默认链接分类", -1, "10/", Int.MAX_VALUE).apply { id = 10 }
val AllFirstParentDBCategoryGroup by lazy { arrayMapof(1 to MovieCategory, 2 to ActressCategory, 10 to LinkCategory) }