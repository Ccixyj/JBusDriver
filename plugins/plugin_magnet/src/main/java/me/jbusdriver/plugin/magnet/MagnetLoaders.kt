package me.jbusdriver.plugin.magnet

import me.jbusdriver.plugin.magnet.loaderImpl.*

object MagnetLoaders {
    /**  "btso.pw" to BtsoPWMagnetLoaderImpl()
     */
    val Loaders: Map<String, IMagnetLoader> by lazy {
        mapOf("Kitty" to CNBtkittyMangetLoaderImpl(), "btdigg" to BtdiggsMagnetLoaderImpl(), "BTSO.PW" to BtsoPWMagnetLoaderImpl(), "TorrentKitty" to TorrentKittyMangetLoaderImpl(), "BTSOW" to BTSOWMagnetLoaderImpl(), "BTDB" to BTDBMagnetLoaderImpl(), "Btanv" to BtanvMagnetLoaderImpl())
    }

}