package me.jbusdriver.http

import android.content.Context
import android.net.ConnectivityManager
import jbusdriver.me.jbusdriver.BuildConfig
import me.jbusdriver.common.AppContext
import me.jbusdriver.common.KLog
import okhttp3.*
import okio.BufferedSink
import okio.GzipSink
import okio.Okio
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.io.File
import java.io.IOException
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit


/**
 * Created by Administrator on 2016/7/22 0022.
 */
object NetClient {
    private val TAG = "NetClient"
    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 5.1.1; Nexus 5 Build/LMY48B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/43.0.2357.65 Mobile Safari/537.36"
    // private val gsonConverterFactory = GsonConverterFactory.create(AppContext.gson)
    private val rxJavaCallAdapterFactory = RxJava2CallAdapterFactory.create()
    private val REWRITE_CACHE_CONTROL_INTERCEPTOR by lazy {
        Interceptor { chain ->
            //方案二：无网读缓存，有网根据过期时间重新请求
            //http://www.jianshu.com/p/2710ed1e6b48
            KLog.t(TAG).i("NetClient: Check network state ")
            val netWorkConection = isNetAvailable(AppContext.Companion.instace)
            var request = chain.request()
            if (!netWorkConection) {
                KLog.t(TAG).i("NO NetWork , FORCE_CACHE")
                request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build()
            } else {
                KLog.t(TAG).i("Has NetWork , go on")
                request = request.newBuilder().header("User-Agent", USER_AGENT).build()
            }

            val response = chain.proceed(request)
            if (netWorkConection) {
                //有网的时候读接口上的@Headers里的配置，你可以在这里进行统一的设置
                //@Headers("Cache-Control: max-age=640000")
                val cacheControl =
                response.newBuilder().removeHeader("Pragma")// 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                        .header("Cache-Control", request.cacheControl().toString()).build()
            } else {
                val maxStale = 60 * 60 * 24 * 7 //一周
                response.newBuilder().removeHeader("Pragma").header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale).build()
            }
            response
        }
    }

    fun getRetrofit(baseUrl: String) = Retrofit.Builder().client(okHttpClient).baseUrl(baseUrl)
            .addConverterFactory(object : Converter.Factory() {
                override fun responseBodyConverter(type: Type?, annotations: Array<out Annotation>?, retrofit: Retrofit?): Converter<ResponseBody, *> {
                     return Converter<ResponseBody, String> { it.string() }
                }


            })
            .addCallAdapterFactory(rxJavaCallAdapterFactory).build()

    //endregion
    private fun gzip(body: RequestBody): RequestBody {
        return object : RequestBody() {
            override fun contentType(): MediaType {
                KLog.d("contentType : gzip!  ")
                return body.contentType()
            }

            @Throws(IOException::class)
            override fun contentLength(): Long {
                return -1
            }

            @Throws(IOException::class)
            override fun writeTo(sink: BufferedSink) {
                val gzipSink = Okio.buffer(GzipSink(sink))
                body.writeTo(gzipSink)
                gzipSink.close()
            }
        }
    }


    val okHttpClient by lazy {
        //设置缓存路径
        val httpCacheDirectory = File(AppContext.Companion.instace.cacheDir, "OK_HTTP_CACHE")
        //设置缓存 100M
        val cache = Cache(httpCacheDirectory, 100 * 1024 * 1024.toLong())

        val client = OkHttpClient.Builder()
                .writeTimeout((30 * 1000).toLong(), TimeUnit.MILLISECONDS)
                .readTimeout((20 * 1000).toLong(), TimeUnit.MILLISECONDS)
                .connectTimeout((15 * 1000).toLong(), TimeUnit.MILLISECONDS)
                .cache(cache)
                .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)

        if (BuildConfig.DEBUG) {
            client.addInterceptor(LoggerInterceptor("OK_HTTP"))
        }
        client.build()
    }


    /**
     * 判断是否有网络可用

     * @param context
     * *
     * @return
     */
    fun isNetAvailable(context: Context): Boolean = try {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.activeNetworkInfo?.isAvailable ?: false
    } catch (e: Exception) {
        false
    }
}
