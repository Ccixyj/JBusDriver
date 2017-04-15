package me.jbusdriver.http

import io.reactivex.Flowable
import me.jbusdriver.common.AppContext.Companion.JBusInstances
import me.jbusdriver.common.KLog
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
        var defaultFastUrl = "https://www.javbus3.com"
        var INSTANCE = getInstance(defaultFastUrl)
        fun getInstance(source: String): JAVBusService {
            KLog.d("instances : $JBusInstances")
            //JBusInstances[type] 会出异常
            return JBusInstances.get(source) ?: createService(source).apply {
                JBusInstances.put(source, this)
                KLog.d("instances : $JBusInstances")
            }
        }

        private fun createService(url: String) = NetClient.getRetrofit(url).create(JAVBusService::class.java)
    }
}