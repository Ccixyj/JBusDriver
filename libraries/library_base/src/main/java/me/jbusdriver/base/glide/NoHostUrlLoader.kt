package me.jbusdriver.base.glide

import com.bumptech.glide.integration.okhttp3.OkHttpStreamFetcher
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import me.jbusdriver.base.KLog
import me.jbusdriver.base.http.NetClient
import me.jbusdriver.base.urlPath
import java.io.InputStream


class GlideNoHostUrl(val url: String, private val noFilterHost:Set<String>) {

    private fun isNeedFilter(url: String) = noFilterHost.any { url.endsWith(it) }
    fun getId() = ((if (isNeedFilter(url)) url else url.urlPath))  /*.apply { KLog.t("GlideNoHostUrl").d("${toStringUrl()} :$this") }*/
    override fun toString(): String = "GlideNoHostUrl(url='$url') ,id =${getId()}"
}

class NoHostImageLoader(private val fac: okhttp3.Call.Factory) : ModelLoader<GlideNoHostUrl, InputStream> {
    override fun buildLoadData(model: GlideNoHostUrl, width: Int, height: Int, options: Options): ModelLoader.LoadData<InputStream>? {
        val gUrl = object : GlideUrl(model.url) {
            override fun getCacheKey(): String = model.getId()
        }
//        KLog.d("load for url $model -> $gUrl")
        return ModelLoader.LoadData(gUrl, OkHttpStreamFetcher(fac, gUrl))
    }

    override fun handles(model: GlideNoHostUrl) = true

    class Factory : ModelLoaderFactory<GlideNoHostUrl, InputStream> {
        override fun build(multiFactory: MultiModelLoaderFactory) =
                NoHostImageLoader(fac = NetClient.glideOkHttpClient)

        override fun teardown() {
        }
    }
}


