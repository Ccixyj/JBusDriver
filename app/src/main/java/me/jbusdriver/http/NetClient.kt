package me.jbusdriver.http

import android.content.Context
import android.net.ConnectivityManager
import android.text.TextUtils
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
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by Administrator on 2016/7/22 0022.
 */
object NetClient {
    private val TAG = "NetClient"
    const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36"
    // private val gsonConverterFactory = GsonConverterFactory.create(AppContext.gson)
    private val rxJavaCallAdapterFactory = RxJava2CallAdapterFactory.create()
    private val EXIST_MAGNET_INTERCEPTOR by lazy {
        Interceptor { chain ->
            KLog.t(TAG).i("NetClient: check is existmag ")
            var request = chain.request()
            val builder = request.newBuilder().header("User-Agent", USER_AGENT)
            if (!TextUtils.isEmpty(request.header("existmag")))
                builder.addHeader("Cookie", "existmag=all")
            request = builder.build()
            chain.proceed(request)
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
                .addNetworkInterceptor(EXIST_MAGNET_INTERCEPTOR)
                .cookieJar(object : CookieJar {
                    private val cookieStore = HashMap<HttpUrl, List<Cookie>>()

                    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                        cookieStore.put(url, cookies)
                    }

                    override fun loadForRequest(url: HttpUrl): List<Cookie> {
                        return cookieStore[url] ?: ArrayList<Cookie>()
                    }
                })
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
