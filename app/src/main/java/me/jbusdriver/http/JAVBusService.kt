package me.jbusdriver.http

import io.reactivex.Flowable
import retrofit2.http.GET
import retrofit2.http.Url
import kotlin.properties.Delegates

/**
 * Created by Administrator on 2017/4/8.
 */
interface JAVBusService {

    //https://announce.javbus8.com/website.php
    @GET
    fun get(@Url url: String): Flowable<String>


    companion object {
        const val annonceurl = "https://announce.javbus8.com/website.php"
        var INSTANCE by Delegates.vetoable(createService("https://announce.javbus8.com/")) {
            _, old, new ->
            true
        }

        fun createService(url: String) = NetClient.getRetrofit(url).create(JAVBusService::class.java)
    }
}