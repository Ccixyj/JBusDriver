package me.jbusdriver.component.magnet

import com.wlqq.phantom.communication.PhantomServiceManager
import com.wlqq.phantom.library.PhantomCore
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.base.JBusManager
import me.jbusdriver.base.KLog
import me.jbusdriver.base.phantom.installAssetsPlugins
import me.jbusdriver.plugin.magnet.common.loader.IMagnetLoader
import java.util.concurrent.TimeUnit

object MagnetPluginHelper {

    const val PluginMagnetPackage = "me.jbusdriver.plugin.magnet"
    // 插件 Phantom Service 的 'NAME'
    const val MagnetService = "MagnetService"
    const val MagnetJavaService = "MagnetJavaService"
    val MagnetLoaders = mutableMapOf<String, IMagnetLoader>()

    private val plugin
        get() = PhantomCore.getInstance().findPluginInfoByPackageName(PluginMagnetPackage)

    private val rxManager by lazy { CompositeDisposable() }

    fun init(){
        installAssetsPlugins(JBusManager.context.assets, "plugins")
                .map {
                    it.find { it.packageName == PluginMagnetPackage }
                            ?: error("not find magnet plugin")
                    (call("getAllLoaders") as? Map<String, IMagnetLoader>)?.onEach { t ->
                        MagnetLoaders.put(t.key, t.value)
                    }
                }.timeout(30, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).subscribe({
                    KLog.d("install plutin success -> $plugin")
                }, {
                    KLog.w("install plutin error -> $it")
                }).addTo(rxManager)
    }

    fun call(method: String, service: String = MagnetJavaService, vararg p: Any): Any? {
        plugin?.let {
            // 插件 Phantom Service 代理对象
            val service = PhantomServiceManager.getService(PluginMagnetPackage, service)
            if (service == null) {
                KLog.w("not find service ")
                return@let
            }
            try {
                val res = service.call(method, *p)
                KLog.d("call $method form service $service result $res")
                return res
            } catch (e: Exception) {
                KLog.w("call $method form service $service error $e")
            }


        } ?: kotlin.run {
            KLog.w("not find plugin info")
        }
        return null
    }

    init {

    }


}