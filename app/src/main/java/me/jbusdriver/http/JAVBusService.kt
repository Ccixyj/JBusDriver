package me.jbusdriver.http

import io.reactivex.Flowable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

/**
 * Created by Administrator on 2017/4/8.
 */
interface JAVBusService {

    //https://announce.javbus8.com/website.php
    @GET
    fun get(@Url url: String): Flowable<String>

    @GET("page/{pageNum}")
    fun getHomePage(@Path("pageNum") page: Int): Flowable<String>


    companion object {
        const val annonceurl = "https://announce.javbus8.com/website.php"
        var defaultFastUrl = "https://www.javbus3.com"
        var INSTANCE = createService(defaultFastUrl)

        fun createService(url: String) = NetClient.getRetrofit(url).create(JAVBusService::class.java)!!
    }
}