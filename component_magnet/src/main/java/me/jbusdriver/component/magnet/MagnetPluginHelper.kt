package me.jbusdriver.component.magnet

import com.wlqq.phantom.library.PhantomCore
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.base.JBusManager
import me.jbusdriver.base.KLog
import me.jbusdriver.base.phantom.installAssetsPlugins
import me.jbusdriver.base.phantom.installFromFile
import me.jbusdriver.base.phantom.pluginServiceCall
import java.io.File
import java.util.concurrent.TimeUnit

object MagnetPluginHelper {

    const val PluginMagnetPackage = "me.jbusdriver.plugin.magnet"
    const val MagnetService = "MagnetService"
    const val MagnetJavaService = "MagnetJavaService"


    private val MagnetLoaders = mutableSetOf<String>()

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

    /**
     * call method throw exception value if null or error
     */
    @Throws
    fun call(method: String, serviceName: String = MagnetService, p: Array<Any> = emptyArray()): Any {
        if (plugin == null) {
            error("call method $method but plugin is null ,is plugin init success?")
        }
        return pluginServiceCall(PluginMagnetPackage, serviceName, method, p)
    }


    fun getLoaderKeys() = kotlin.runCatching {
        if (MagnetLoaders.size > 3) {
            return@runCatching MagnetLoaders.toList()
        }
        (call("getLoaderKeys") as? List<String>)?.onEach { t ->
            KLog.i("find loader $t")
            MagnetLoaders.add(t)
        }
        return@runCatching MagnetLoaders.toList()
    }.getOrNull() ?: MagnetLoaders.toList()

    fun getMagnets(loader: String, key: String, page: Int) = kotlin.runCatching {
        call(method = "getMagnets", p = arrayOf(loader, key, page)).toString()
    }.getOrNull() ?: ""

    fun fetchMagLink(magnetLoaderKey: String, url: String) =
        kotlin.runCatching { call(method = "fetchMagLink", p = arrayOf(magnetLoaderKey, url)).toString() }
            .getOrNull() ?: ""


    fun hasNext(magnetLoaderKey: String) = kotlin.runCatching {
        (call(method = "hasNext", p = arrayOf(magnetLoaderKey)) as? Boolean) ?: false
    }.getOrNull() ?: false

    /**
     * 从file安装apk插件
     */
    fun installApkFile(f: File) = installFromFile(f)


}