package me.jbusdriver.common

import me.jbusdriver.library.image.glide.GlideNoHostUrl
import me.jbusdriver.http.JAVBusService


val String.toGlideNoHostUrl: GlideNoHostUrl
    inline get() = GlideNoHostUrl(this, JAVBusService.xyzHostDomains)

val String.isEndWithXyzHost
    get() = JAVBusService.xyzHostDomains.any { this.endsWith(it) }