package me.jbusdriver.common.bean.plugin

class Plugins {
    val internal: List<PluginBean> = emptyList()
    override fun toString(): String {
        return "Plugins(internal=$internal)"
    }
}