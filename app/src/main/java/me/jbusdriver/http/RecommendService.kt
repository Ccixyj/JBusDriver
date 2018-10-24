package me.jbusdriver.http

import com.google.gson.JsonObject
import io.reactivex.Flowable
import me.jbusdriver.base.http.NetClient
import retrofit2.http.*

/**
 * Created by Administrator on 2017/4/15 0015.
 */
@Deprecated("not use any more")
interface RecommendService {

    @GET("v1/api/recommends/{count}")
    fun recommends(@Path("count") count: Int = 1): Flowable<JsonObject>


    @FormUrlEncoded
    @PUT("v1/api/recommend")
    fun putRecommends(@FieldMap fields: Map<String, String>): Flowable<JsonObject>

    companion object {
//        val INSTANCE by lazy { NetClient.getRetrofit("http://192.168.1.100:8001/"/*"http://qclxyj.com/"*/, true).create(RecommendService::class.java) }
        val INSTANCE by lazy { NetClient.getRetrofit("http://qclxyj.com/", true).create(RecommendService::class.java) }
    }
}