package me.jbusdriver.common

import com.bumptech.glide.load.model.GlideUrl
import me.jbusdriver.base.urlHost
import me.jbusdriver.base.urlPath

class GlideNoHost(url: String) : GlideUrl(url) {

    override fun getCacheKey() = let {
        val url = toStringUrl()
        ((if (url.urlHost.endsWith("xyz")) url else url.urlPath))  /*.apply { KLog.t("GlideNoHost").d("${toStringUrl()} :$this") }*/
    }
}

/*glide : url ->? custome glideurl */
val String.toGlideUrl: GlideNoHost
    inline get() = GlideNoHost(this) /**/