package me.jbusdriver.base.phantom

import android.content.res.AssetManager
import com.tbruyelle.rxpermissions2.RxPermissions
import com.wlqq.phantom.communication.PhantomServiceManager
import com.wlqq.phantom.library.PhantomCore
import com.wlqq.phantom.library.pm.PluginInfo
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.base.KLog
import java.io.File
import java.util.concurrent.TimeUnit


fun installAssetsPlugins(assets: AssetManager, dir: String): Flowable<List<PluginInfo>> {
    val f = assets.list(dir) ?: emptyArray()
    KLog.i("start installAssetsPlugins $assets  ---> $dir ---->${f.joinToString()}")
    return Flowable.just(f)
            .map {
                return@map it.mapNotNull { file ->
                    if (file.endsWith(".apk")) {
                        val filePath = "$dir/$file"
                        val installResult = PhantomCore.getInstance().installPluginFromAssets(filePath)
                        if (installResult.isSuccess && installResult.plugin != null) {
                            installResult.plugin?.start()
                            installResult.plugin
                        } else {
                            // should not happen
                            throw error("install success , but plugin is null!!!!")
                        }
                    } else null
                }.apply {
                    if (this.isNotEmpty()) {
                        KLog.d("install success $this")
                    } else error("not find any plugins to install")
                }

            }.subscribeOn(Schedulers.io()).timeout(10, TimeUnit.SECONDS)
}

/**
 * 当前及sub
 *  *** 如果使用sd卡,需要获取外置sd权限***
 * @param f directory
 */
fun installFromPathDir(f: File): Flowable<List<PluginInfo>> {
    if (f.exists() && !f.isDirectory) {
        f.deleteRecursively()
    }
    if (!f.exists()) {
        f.mkdirs()
        return Flowable.just(emptyList())
    }
    return Flowable.fromCallable {

        f.walkTopDown().filter {
            it.name.endsWith(".apk")
        }.mapNotNull {
            val installResult = PhantomCore.getInstance().installPlugin(it.absolutePath)
            if (installResult.isSuccess && installResult.plugin != null) {
                installResult.plugin?.start()
                installResult.plugin
            } else {
                // should not happen
                throw error("install success , but plugin is null!!!!")
            }
        }.toList().apply {
            KLog.d("install success $this")
        }

    }.subscribeOn(Schedulers.io()).timeout(10, TimeUnit.SECONDS)

}


/**
 * 当前及sub
 * @param f apk
 */
fun installFromFile(f: File): Flowable<PluginInfo> {
    require(f.exists())
    return Flowable.fromCallable {
        val installResult = PhantomCore.getInstance().installPlugin(f.absolutePath)
        val plugin = installResult.plugin
        if (installResult.isSuccess) {
            plugin?.start() ?: throw error("install success , but plugin is null!!!!")
            plugin
        } else {
            // should not happen
            throw error("install failed : $installResult")
        }
    }.subscribeOn(Schedulers.io()).timeout(10, TimeUnit.SECONDS)

}


/**
 * call method throw exception value if null or error
 */
@Throws
fun pluginServiceCall(packageName: String, serviceName: String, method: String, vararg p: Any): Any {
    // 插件 Phantom Service 代理对象
    val service = PhantomServiceManager.getService(packageName, serviceName)
    if (service == null) {
        KLog.w("not find service")
        error("call method $method but plugin is null")
    }
    try {
        val res = service.call(method, *p)
        KLog.d("call $method form service $service result $res")
        if (res == null) error("call method $method but plugin is null")
        return res
    } catch (e: Exception) {
        error(e)
    }
}