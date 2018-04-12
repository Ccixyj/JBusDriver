package me.jbusdriver.http

import io.reactivex.Flowable
import retrofit2.http.*

/**
 * Created by Administrator on 2017/4/15 0015.
 */
interface Recommend {

    @GET("v1/api/recommends/{count}")
    fun recommends(@Path("count") count: Int = 1): Flowable<String>


    @FormUrlEncoded
    @PUT("v1/api/recommend")
    fun putRecommends(@FieldMap fields: Map<String, String>): Flowable<String>

    companion object {
        val INSTANCE by lazy { NetClient.getRetrofit("http://qclxyj.com/", true).create(Recommend::class.java) }
    }
}