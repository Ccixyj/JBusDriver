package me.jbusdriver.mvp.bean.plugin

data class PluginBean(val versionCode: Int, val versionName: String, val url: String, val tag: String, val desc: String, val eTag: String) : Comparable<PluginBean> {

    override operator fun compareTo(other: PluginBean) = this.versionCode - other.versionCode
}