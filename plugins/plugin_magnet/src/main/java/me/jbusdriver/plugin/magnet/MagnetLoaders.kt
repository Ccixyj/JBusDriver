package me.jbusdriver.plugin.magnet

import me.jbusdriver.plugin.magnet.common.loader.IMagnetLoader

object MagnetLoaders {
    /**  "btso.pw" to BtsoPWMagnetLoaderImpl()
     */
    val Loaders: Map<String, IMagnetLoader> by lazy {
        mapOf("BTDB" to me.jbusdriver.plugin.magnet.loaderImpl.BTDBMagnetLoaderImpl(), "btdigg" to me.jbusdriver.plugin.magnet.loaderImpl.BtdiggsMagnetLoaderImpl(), "BTSO.PW" to me.jbusdriver.plugin.magnet.loaderImpl.BtsoPWMagnetLoaderImpl(), "TorrentKitty" to me.jbusdriver.plugin.magnet.loaderImpl.TorrentKittyMangetLoaderImpl(), "BTSOW" to me.jbusdriver.plugin.magnet.loaderImpl.BTSOWMagnetLoaderImpl(), "Kitty" to me.jbusdriver.plugin.magnet.loaderImpl.CNBtkittyMangetLoaderImpl(), "Btanv" to me.jbusdriver.plugin.magnet.loaderImpl.BtanvMagnetLoaderImpl())
    }

}