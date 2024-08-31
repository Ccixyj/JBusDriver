package me.jbusdriver.http

import io.reactivex.Flowable
import me.jbusdriver.base.KLog
import me.jbusdriver.base.http.NetClient
import me.jbusdriver.common.JBus
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

/**
 * Created by Administrator on 2017/4/8.
 */
interface JAVBusService {


    //https://announce.javbus8.com/website.php
    @GET
    fun get(@Url url: String, @Header("existmag") existmag: String = ""): Flowable<String>



    companion object {
        var defaultFastUrl = "https://www.seedmm.life"
        var defaultXyzUrl = "https://www.javbus.one"
        val xyzHostDomains by lazy {
            mutableSetOf<String>().apply {
                this.add(defaultXyzUrl.takeLast(defaultXyzUrl.lastIndexOf(".").coerceAtLeast(0)))
            }
        }


        var INSTANCE = getInstance(defaultFastUrl)
        fun getInstance(source: String): JAVBusService {
            //JBusServices[type] 会出异常
            return JBus.JBusServices.getOrPut(source) {
                createService(source)
            }.apply {
                KLog.d("instances : ${JBus.JBusServices}, defaultFastUrl : $defaultFastUrl")
            }
        }

        private fun createService(url: String) =
            NetClient.getRetrofit("${url.trimEnd('/')}/").create(JAVBusService::class.java)

    }
}