package me.jbusdriver.mvp.bean

/**
 * Created by Administrator on 2017/7/29.
 */

data class SearchWord(val query: String)

@Deprecated("not use ")
data class CollectErrorEvent(val key: String, val msg: String)


//config
data class PageChangeEvent(val mode: Int)

class MenuChangeEvent

class CategoryChangeEvent
