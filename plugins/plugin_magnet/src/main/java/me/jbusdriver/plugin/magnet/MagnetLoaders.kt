package me.jbusdriver.plugin.magnet

import me.jbusdriver.plugin.magnet.loaders.*

object MagnetLoaders {
    /**  "btso.pw" to BtsoPWMagnetLoaderImpl()
     */
    val Loaders: Map<String, IMagnetLoader> by lazy {
        mapOf(
            "Kitty" to CNBtkittyMangetLoaderImpl(),
            "btdigg" to BtdiggsMagnetLoaderImpl(),
            "zzjd" to ZZJDMagnetLoaderImpl(),
            "BTbaocai" to BTBCMagnetLoaderImpl(),
            "Btanv" to BtanvMagnetLoaderImpl(),
            "BTSO.PW" to BtsoPWMagnetLoaderImpl(),
            "BTSOW" to BTSOWMagnetLoaderImpl(),
           /* "BTDB" to BTDBMagnetLoaderImpl(),*/
            "btcherries" to BTCherryMagnetLoaderImpl(),
            "TorrentKitty" to TorrentKittyMangetLoaderImpl()

        )
    }

}