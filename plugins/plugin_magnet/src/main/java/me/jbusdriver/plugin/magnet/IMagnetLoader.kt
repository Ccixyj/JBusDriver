package me.jbusdriver.plugin.magnet

import android.util.Base64

import org.json.JSONObject
import org.jsoup.Connection
import java.util.zip.*

const val USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.67 Safari/537.36"

interface IMagnetLoader {


    /**
     * 是否有下一页
     */
    var hasNexPage: Boolean

    /**
     * 放入后台线程执行
     */
    fun loadMagnets(key: String, page: Int): List<JSONObject>


    fun Connection.initHeaders(): Connection = this.userAgent(USER_AGENT).followRedirects(true)
        .header("Accept-Encoding", "gzip, deflate, sdch")
        .header("Accept-Language", "zh-CN,zh;q=0.8")

    fun fetchMagnetLink(url: String): String = ""


    companion object {
        const val MagnetFormatPrefix = "magnet:?xt=urn:btih:"

    }

}
