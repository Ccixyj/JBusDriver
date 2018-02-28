package me.jbusdriver.http

import android.content.Context
import android.net.ConnectivityManager
import android.text.TextUtils
import com.facebook.stetho.okhttp3.StethoInterceptor
import jbusdriver.me.jbusdriver.BuildConfig
import me.jbusdriver.common.GlideProgressListener
import me.jbusdriver.common.JBus
import me.jbusdriver.common.KLog
import me.jbusdriver.common.ProgressResponseBody
import okhttp3.*
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.io.File
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by Administrator on 2016/7/22 0022.
 */
object NetClient {
    private const val TAG = "NetClient"
    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36"
    // private val gsonConverterFactory = GsonConverterFactory.create(GSON)
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

    private val PROGRESS_INTERCEPTOR by lazy {
        Interceptor { chain ->
            KLog.t(TAG).i("progress start ")
            val request = chain.request()
            val response = chain.proceed(request)
             response.newBuilder()
                    .body(ProgressResponseBody(request.url().toString(), response.body(), GlideProgressListener))
                    .build()
        }
    }


    fun getRetrofit(baseUrl: String) = Retrofit.Builder().client(okHttpClient).baseUrl(baseUrl)
            .addConverterFactory(object : Converter.Factory() {
                override fun responseBodyConverter(type: Type?, annotations: Array<out Annotation>?, retrofit: Retrofit?): Converter<ResponseBody, *> = Converter<ResponseBody, String> { it.string() }
            })
            .addCallAdapterFactory(rxJavaCallAdapterFactory).build()!!

    //endregion

    val okHttpClient by lazy {
        //设置缓存路径
        val httpCacheDirectory = File(JBus.cacheDir, "OK_HTTP_CACHE")
        //设置缓存 100M
        val cache = Cache(httpCacheDirectory, 100 * 1024 * 1024.toLong())

        val client = OkHttpClient.Builder()
                .writeTimeout((30 * 1000).toLong(), TimeUnit.MILLISECONDS)
                .readTimeout((20 * 1000).toLong(), TimeUnit.MILLISECONDS)
                .connectTimeout((15 * 1000).toLong(), TimeUnit.MILLISECONDS)
                .cache(cache)
                .addNetworkInterceptor(EXIST_MAGNET_INTERCEPTOR)
                .addNetworkInterceptor(StethoInterceptor())
                .cookieJar(object : CookieJar {
                    private val cookieStore = HashMap<HttpUrl, List<Cookie>>()

                    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                        cookieStore[url] = cookies
                    }

                    override fun loadForRequest(url: HttpUrl) = cookieStore[url] ?: ArrayList()
                })
        if (BuildConfig.DEBUG) {
            client.addInterceptor(LoggerInterceptor("OK_HTTP"))
        }
        client.build()
    }

    val glideOkHttpClient by lazy {
        val client = OkHttpClient.Builder()
                .readTimeout((20 * 1000).toLong(), TimeUnit.MILLISECONDS)
                .connectTimeout((15 * 1000).toLong(), TimeUnit.MILLISECONDS)
                .addNetworkInterceptor(StethoInterceptor())
                .addNetworkInterceptor(PROGRESS_INTERCEPTOR)
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
