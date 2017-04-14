package me.jbusdriver.ui.data

/**
 * Created by Administrator on 2017/4/14 0014.
 */
enum class DataSourceType(val key: String) {
    CENSORED("censored"),
    UNCENSORED("uncensored"),
    GENRE("genre"),
    UNCENSORED_GENRE("/uncensored/genre"),
    ACTRESSES("actresses"),
    UNCENSORED_ACTRESSES("/uncensored/actresses"),
    XYZ("xyz/"),
    GENRE_HD("/genre/hd");
}