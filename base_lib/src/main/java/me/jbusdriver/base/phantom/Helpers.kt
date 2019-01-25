package me.jbusdriver.base.phantom

import android.content.res.AssetManager
import com.wlqq.phantom.library.PhantomCore
import com.wlqq.phantom.library.pm.PluginInfo
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.base.KLog
import java.io.File


fun installAssetsPlugins(assets: AssetManager, dir: String): Flowable<List<PluginInfo>> {
    val f =assets.list(dir)
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
                    KLog.d("install success $this")
                }

            }.subscribeOn(Schedulers.io())
}

/**
 * 当前及sub
 * @param f directory
 */
fun installFromPathDir(f: File): Flowable<List<PluginInfo>> {
    require(f.exists())
    require(f.isDirectory)
    return Flowable.fromCallable {
        f.walkTopDown().filter {
            it.endsWith(".apk")
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

    }.subscribeOn(Schedulers.io())

}