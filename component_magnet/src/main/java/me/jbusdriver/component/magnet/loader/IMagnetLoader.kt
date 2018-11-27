package me.jbusdriver.component.magnet.loader

import android.util.Base64
import me.jbusdriver.base.http.NetClient
import me.jbusdriver.component.magnet.bean.Magnet
import org.jsoup.Connection


interface IMagnetLoader {




    /**
     * 是否有下一页
     */
    var hasNexPage: Boolean

    /**
     * 放入后台线程执行
     */
    fun loadMagnets(key: String, page: Int): List<Magnet>

    fun encode(string: String) = Base64.encodeToString(string.toByteArray(), Base64.NO_PADDING or Base64.URL_SAFE).trim()
    fun Connection.initHeaders() :Connection = this.userAgent(NetClient.USER_AGENT).followRedirects(true)
            .header("Accept-Encoding", "gzip, deflate, sdch")
            .header("Accept-Language", "zh-CN,zh;q=0.8")


    companion object {
        const val MagnetFormatPrefix = "magnet:?xt=urn:btih:"



        /**  "btso.pw" to BtsoPWMagnetLoaderImpl()
         */
        val MagnetLoaders: Map<String, IMagnetLoader> by lazy {
            mapOf("BTDB" to BTDBMagnetLoaderImpl(),"btdigg" to BtdiggsMagnetLoaderImpl(), "BTSO.PW" to BtsoPWMagnetLoaderImpl(), "TorrentKitty" to TorrentKittyMangetLoaderImpl(), "BTSOW" to BTSOWMagnetLoaderImpl(), "CNBtkitty" to CNBtkittyMangetLoaderImpl(), "Btanv" to BtanvMagnetLoaderImpl())
        }
    }

}
