package me.jbusdriver.http

import io.reactivex.Flowable
import me.jbusdriver.base.http.NetClient
import retrofit2.http.GET
import retrofit2.http.Url
import okhttp3.ResponseBody
import retrofit2.http.Streaming



/**
 * Created by Administrator on 2017/4/15 0015.
 */
interface GitHub {
    @GET("https://raw.githubusercontent.com/Ccixyj/JBusDriver/master/api/announce.json")
    fun announce(): Flowable<String>


    @GET("https://raw.githubusercontent.com/Ccixyj/JBusDriver/plugin/api/announce.json")
    fun announceWithPlugin(): Flowable<String>

    @Streaming
    @GET
    fun downloadPluginAsync(@Url fileUrl: String): Flowable<ResponseBody>

    companion object{
        val INSTANCE by lazy { NetClient.getRetrofit("https://raw.githubusercontent.com/").create(GitHub::class.java) }
    }
}