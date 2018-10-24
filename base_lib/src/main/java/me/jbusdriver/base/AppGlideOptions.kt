package me.jbusdriver.base

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import me.jbusdriver.base.glide.GlideNoHostUrl
import me.jbusdriver.base.glide.NoHostImageLoader
import java.io.InputStream


@GlideModule
open class AppGlideOptions : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        if (BuildConfig.DEBUG) builder.setLogLevel(Log.VERBOSE)
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(GlideNoHostUrl::class.java, InputStream::class.java,
                NoHostImageLoader.Factory())
    }


}



