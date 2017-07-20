package me.jbusdriver.ui.data

/**
 * Created by Administrator on 2017/7/20.
 */

enum class SearchType(val suffix: String, val urlPath: String = "/"){
    CENSORED("有碼影片","/search/%s"),
    UNCENSORED("無碼影片","/uncensored/search/%s"),
    ACTRESS("女優","/searchstar/"),
    DIRECTOR("導演","/search/%s&amp;type=2"),
    MAKER("製作商","/search/%s&amp;type=3"),
    PUBLISHER("發行商","/search/%s&amp;type=4"),
    SERIES("系列","/search/%s&amp;type=5")
}