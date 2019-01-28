package me.jbusdriver.component.magnet

import com.wlqq.phantom.communication.PhantomServiceManager
import com.wlqq.phantom.library.PhantomCore
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.base.JBusManager
import me.jbusdriver.base.KLog
import me.jbusdriver.base.phantom.installAssetsPlugins
import me.jbusdriver.base.phantom.installFromFile
import java.io.File
import java.util.concurrent.TimeUnit

object MagnetPluginHelper {

    const val PluginMagnetPackage = "me.jbusdriver.plugin.magnet"
    // 插件 Phantom Service 的 'NAME'
    const val MagnetService = "MagnetService"
    const val MagnetJavaService = "MagnetJavaService"
    val MagnetLoaders = mutableListOf<String>()

    private val plugin
        get() = PhantomCore.getInstance().findPluginInfoByPackageName(PluginMagnetPackage)

    private val rxManager by lazy { CompositeDisposable() }

    fun init() {
        if (!PhantomCore.getInstance().isPluginInstalled(PluginMagnetPackage)) {
            installAssetsPlugins(JBusManager.context.assets, "plugins")
                    .timeout(30, TimeUnit.SECONDS)
                    .doOnNext {
                        getLoaderKeys()
                    }.subscribeOn(Schedulers.io()).subscribe({
                        KLog.d("install  success -> $plugin")
                    }, {
                        KLog.w("install  error -> $it")
                    }).addTo(rxManager)
            return
        }
        //installed
        //check start
        PhantomCore.getInstance().allPlugins.filter { !it.isStarted }.forEach {
            try {
                it.start()
            } catch (e: Exception) {
                e.printStackTrace()
                KLog.e("plugin $it can not start!!!")
            }
        }

    }

    fun call(method: String, service: String = MagnetService, vararg p: Any): Any? {
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


    fun getLoaderKeys() = kotlin.run {
        if (MagnetLoaders.isNotEmpty()) {
            return@run MagnetLoaders.toList()
        }
        (call("getLoaderKeys") as? List<String>)?.onEach { t ->
            KLog.i("find loader $t")
            MagnetLoaders.add(t)
        }
        return@run MagnetLoaders.toList()
    }

    fun getMagnets(loader: String, key: String, page: Int): String {
        return try {
            call(method = "getMagnets", p = *arrayOf(loader, key, page)).toString()
        } catch (e: Exception) {
            KLog.w("getMagnets error $e")
            ""
        }
    }

    fun fetchMagLink(magnetLoaderKey: String, url: String): String {
        return try {
            call(method = "fetchMagLink", p = *arrayOf(magnetLoaderKey, url)).toString()
        } catch (e: Exception) {
            KLog.w("fetchMagLink error $e")
            ""
        }
    }


    fun hasNext(magnetLoaderKey: String): Boolean {
        return try {
            (call(method = "hasNext", p = *arrayOf(magnetLoaderKey)) as? Boolean) ?: false
        } catch (e: Exception) {
            KLog.w("fetchMagLink error $e")
            false
        }
    }

    fun update(f: File) =installFromFile(f)


}