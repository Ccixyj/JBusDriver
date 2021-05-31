package me.jbusdriver.common

import me.jbusdriver.base.glide.GlideNoHostUrl
import me.jbusdriver.http.JAVBusService


val String.toGlideNoHostUrl: GlideNoHostUrl
    inline get() = GlideNoHostUrl(this, JAVBusService.xyzHostDomains, JAVBusService.defaultFastUrl)

val String.isEndWithXyzHost
    get() = JAVBusService.xyzHostDomains.any { this.endsWith(it) }