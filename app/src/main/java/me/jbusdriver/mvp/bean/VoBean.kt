package me.jbusdriver.mvp.bean

data class ResultPageBean<T>(val pageInfo: PageInfo, val data: List<T>) {

    companion object {
        fun <T> emptyPage(pageInfo: PageInfo) = ResultPageBean(pageInfo, emptyList<T>())
    }
}

