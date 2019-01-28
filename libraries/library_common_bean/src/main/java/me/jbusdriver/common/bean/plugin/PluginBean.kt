package me.jbusdriver.common.bean.plugin

import com.wlqq.phantom.library.pm.PluginInfo

data class PluginBean(val name: String ,val versionCode: Int, val versionName: String,  val desc: String, val eTag: String ,val url: String) : Comparable<PluginBean> {

    override operator fun compareTo(other: PluginBean) = this.versionCode - other.versionCode

}


fun PluginInfo.toPluginBean() = PluginBean(name = this.packageName,versionCode =  this.versionCode , versionName =  this.versionName ,
        desc =  this.toString() , eTag = "" , url = "")
