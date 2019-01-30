package me.jbusdriver.component.magnet

import com.billy.cc.core.component.CC
import com.billy.cc.core.component.CCResult
import com.billy.cc.core.component.IComponent

import me.jbusdriver.base.KLog
import me.jbusdriver.base.common.C
import me.jbusdriver.component.magnet.ui.activity.MagnetPagerListActivity
import me.jbusdriver.component.magnet.ui.config.Configuration

class ComponentMagnet : IComponent {


    override fun getName() = C.Components.Magnet

    override fun onCall(cc: CC): Boolean {
        val actionName = cc.actionName
        try {
            when (actionName) {
                "show" -> {
                    val keyWord = cc.getParamItem<String>("keyword")
                            ?: error("show activity must pass keyword")
                    MagnetPagerListActivity.start(cc, keyWord)
                    CC.sendCCResult(cc.callId, CCResult.success())
                }
                "allKeys" -> {
                    CC.sendCCResult(cc.callId, CCResult.success(mapOf("keys" to MagnetPluginHelper.getLoaderKeys())))
                }
                "config.save" -> {
                    val keys = cc.getParamItem<List<String>>("keys")
                            ?: error("call config.save must past keys")
                    Configuration.saveMagnetKeys(keys)
                    CC.sendCCResult(cc.callId, CCResult.success())

                }
                "config.getKeys" -> {
                    CC.sendCCResult(cc.callId, CCResult.success(mapOf("keys" to Configuration.getConfigKeys())))
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

}