package me.jbusdriver.common

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import me.jbusdriver.http.NetClient
import java.io.InputStream

@GlideModule
open class AppGlideOptions : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        if (jbusdriver.me.jbusdriver.BuildConfig.DEBUG) builder.setLogLevel(Log.VERBOSE)
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.replace(GlideUrl::class.java, InputStream::class.java,
                OkHttpUrlLoader.Factory(NetClient.okHttpClient))
    }


}

class GlideNoHost(url: String) : GlideUrl(url) {

    override fun getCacheKey() = toStringUrl().urlPath.apply { KLog.t("GlideNoHost").d(this) }
}

