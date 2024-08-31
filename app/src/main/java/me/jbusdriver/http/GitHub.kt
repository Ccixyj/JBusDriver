package me.jbusdriver.http

import io.reactivex.Flowable
import me.jbusdriver.BuildConfig
import me.jbusdriver.base.http.NetClient
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path


/**
 * Created by Administrator on 2017/4/15 0015.
 */
interface GitHub {
    @Headers("Referer:https://gitlab.com/")
    @GET("https://gitlab.com/Ccixyj/staticFile/-/raw/main/announce.json")
    fun announce(): Flowable<String>


//    @GET("https://cdn.jsdelivr.net/gh/Ccixyj/JBusDriver@master/api/announce.json")
//    @Headers("""sec-ch-ua-platform: "Windows"""" ,
//        "user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36" )
//    fun announce(): Flowable<String>

//
//    @GET("https://raw.githubusercontent.com/Ccixyj/JBusDriver/plugin/api/announce.json")
//    fun announceWithPlugin(): Flowable<String>


    companion object {
        val INSTANCE by lazy { NetClient.getRetrofit("https://gitlab.com/").create(GitHub::class.java) }
    }
}