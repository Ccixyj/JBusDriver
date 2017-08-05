package me.jbusdriver.ui.data

/**
 * Created by Administrator on 2017/4/14 0014.
 */
enum class DataSourceType(val key: String, val prefix: String = "/") {
    CENSORED("有碼", "/page/"), //有码
    GENRE("有碼類別"), //类别
    ACTRESSES("有碼女優"), //女优

    UNCENSORED("無碼", "/page/"), //无码
    UNCENSORED_GENRE("無碼類別"), //无码类别
    UNCENSORED_ACTRESSES("無碼女優"), //无码女优

    XYZ("歐美", "/page/"), //欧美
    XYZ_GENRE("xyz/genre"), //欧美类别
    XYZ_ACTRESSES("xyz/actresses"),


    GENRE_HD("高清"), //高清
    Sub("字幕");//字幕
}