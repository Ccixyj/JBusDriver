package me.jbusdriver.component.magnet

import android.annotation.SuppressLint
import com.billy.cc.core.component.CC
import com.billy.cc.core.component.CCResult
import com.billy.cc.core.component.IComponent
import com.wlqq.phantom.library.PhantomCore
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.base.KLog
import me.jbusdriver.base.common.C
import me.jbusdriver.common.bean.plugin.toPluginBean
import java.io.File
import java.util.concurrent.TimeUnit

class ComponentPluginMagnet : IComponent {

    init {
        MagnetPluginHelper.init()
    }


    override fun getName() = C.PluginComponents.PluginMagnet

    override fun onCall(cc: CC): Boolean {
        val actionName = cc.actionName
        try {
            when (actionName) {
                "plugins.all" -> {
                    getAllPlugin(cc)
                }
                "plugins.install" -> {
                    //async call
                    val pluginPath = cc.getParamItem<String>("path")
                        ?: error("must past apk's path")
                    installPlugin(cc, pluginPath)
                    return true
                }
                else -> {
                    CC.sendCCResult(cc.callId, CCResult.errorUnsupportedActionName())
                }

            }
        } catch (e: Exception) {
            KLog.w("$cc call error $e")
            CC.sendCCResult(cc.callId, CCResult.error(e.message))
        }

        return false
    }

    private fun getAllPlugin(cc: CC) {
        val plugins = PhantomCore.getInstance().allPlugins.map { it.toPluginBean() }
        KLog.d("comp : $name has plugins $plugins")
        CC.sendCCResult(
            cc.callId,
            CCResult.success(mapOf("plugins" to plugins))
        )
    }

    @SuppressLint("CheckResult")
    private fun installPlugin(cc: CC, path: String) {
        MagnetPluginHelper.installApkFile(File(path)).timeout(10, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
            .timeout(10, TimeUnit.SECONDS)
            .filter { !cc.isStopped }
            .subscribe({
                CC.sendCCResult(cc.callId, CCResult.success("plugin", it.toPluginBean()))
            }, {
                CC.sendCCResult(cc.callId, CCResult.error("install error $it"))
            })
    }
}