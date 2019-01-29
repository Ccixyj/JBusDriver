package me.jbusdriver.common.bean.plugin

import com.billy.cc.core.component.CC

/**
 * 组件使用plugin需要实现该接口
 */
interface IPluginComponent {

    /**
     * action : plugins.all
     */
    fun getAllPlugin(cc: CC)

    /**
     * action : plugins.install
     */
    fun installPlugin(cc: CC, path: String)

}