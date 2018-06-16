package me.jbusdriver.http

import io.reactivex.Flowable
import me.jbusdriver.base.http.NetClient
import retrofit2.http.GET

/**
 * Created by Administrator on 2017/4/15 0015.
 */
interface GitHub {
    @GET("https://raw.githubusercontent.com/Ccixyj/JBusDriver/master/api/announce.json")
    fun announce(): Flowable<String>
    companion object{
        val INSTANCE by lazy { NetClient.getRetrofit("https://raw.githubusercontent.com/").create(GitHub::class.java) }
    }
}