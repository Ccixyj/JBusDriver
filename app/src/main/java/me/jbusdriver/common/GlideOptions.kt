package me.jbusdriver.common

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import com.bumptech.glide.module.GlideModule
import me.jbusdriver.http.NetClient
import java.io.InputStream
import java.net.URL

/**
 * Created by Administrator on 2016/7/22 0022.
 */
class GlideOptions : GlideModule {
    override fun applyOptions(context: Context, builder: GlideBuilder) {

    }

    override fun registerComponents(context: Context, glide: Glide) {
        glide.register(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(NetClient.okHttpClient))
    }


}

class GlideNoHost : GlideUrl {
    constructor(url: URL) : super(url)
    constructor(url: String) : super(url)
    constructor(url: URL, headers: Headers?) : super(url, headers)
    constructor(url: String, headers: Headers?) : super(url, headers)

    override fun getCacheKey() = toStringUrl().urlPath
}
