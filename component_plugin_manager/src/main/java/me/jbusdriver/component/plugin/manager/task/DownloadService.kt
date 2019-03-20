package me.jbusdriver.component.plugin.manager.task

import io.reactivex.Flowable
import me.jbusdriver.base.http.NetClient.PROGRESS_INTERCEPTOR
import me.jbusdriver.base.http.NetClient.RxJavaCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

interface DownloadService {


    @Streaming
    @GET
    fun downloadPluginAsync(@Url fileUrl: String): Flowable<ResponseBody>


    companion object {

        private val client = OkHttpClient.Builder()
            .connectTimeout((15 * 1000).toLong(), TimeUnit.MILLISECONDS)
            .addInterceptor(PROGRESS_INTERCEPTOR)
            .build()

        fun createService() =
            Retrofit.Builder().client(client).baseUrl("https://raw.githubusercontent.com").addCallAdapterFactory(
                RxJavaCallAdapterFactory
            ).build().create(DownloadService::class.java)

    }
}