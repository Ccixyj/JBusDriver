package me.jbusdriver.ui.data

/**
 * Created by Administrator on 2017/4/14 0014.
 */
enum class DataSourceType(val key: String) {
    CENSORED("censored"), //有码
    GENRE("genre"), //类别
    ACTRESSES("actresses"), //女优

    UNCENSORED("uncensored"), //无码
    UNCENSORED_GENRE("/uncensored/genre"), //无码类别
    UNCENSORED_ACTRESSES("/uncensored/actresses"), //无码女优



    XYZ_GENRE("xyz/genre"), //欧美类别
    XYZ("xyz/"), //欧美
    XYZ_ACTRESSES("xyz/actresses"),


    GENRE_HD("/genre/hd"), //高清
    Sub("/genre/sub");//字幕
}