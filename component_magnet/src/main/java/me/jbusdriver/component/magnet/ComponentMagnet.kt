package me.jbusdriver.component.magnet

import com.billy.cc.core.component.CC
import com.billy.cc.core.component.CCResult
import com.billy.cc.core.component.IComponent
import me.jbusdriver.base.common.C
import me.jbusdriver.component.magnet.loader.IMagnetLoader.Companion.MagnetLoaders
import me.jbusdriver.component.magnet.ui.activity.MagnetPagerListActivity
import me.jbusdriver.component.magnet.ui.config.Configuration

class ComponentMagnet : IComponent {

    override fun getName() = C.Components.Manget

    override fun onCall(cc: CC): Boolean {
        val actionName = cc.actionName
        when (actionName) {
            "show" -> {
                cc.getParamItem<String>("keyword")?.let {
                    MagnetPagerListActivity.start(cc, it)
                    CC.sendCCResult(cc.callId, CCResult.success())
                } ?: CC.sendCCResult(cc.callId, CCResult.error("show activity must pass keyword"))

            }
            "allKeys" -> {
                CC.sendCCResult(cc.callId, CCResult.success(mapOf("keys" to MagnetLoaders.keys.toList())))
            }
            "config.save" -> {
                cc.getParamItem<List<String>>("keys")?.let {
                    Configuration.saveMagnetKeys(it)
                    CC.sendCCResult(cc.callId, CCResult.success())
                } ?: let {
                    CC.sendCCResult(cc.callId, CCResult.error("call config.save must past keys"))
                }
            }
            "config.getKeys" -> {
                CC.sendCCResult(cc.callId, CCResult.success(mapOf("keys" to Configuration.getConfigKeys())))
            }
        }
        return false
    }
}