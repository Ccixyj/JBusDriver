package me.jbusdriver.http

import io.reactivex.Flowable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

/**
 * Project: JAViewer
 */
interface AvMoService {

    @GET(AvMoService.LANGUAGE_NODE + "/pageInfo/{pageInfo}")
    fun getHomePage(@Path("pageInfo") page: Int): Flowable<String>

    @GET(AvMoService.LANGUAGE_NODE + "/released/pageInfo/{pageInfo}")
    fun getReleased(@Path("pageInfo") page: Int): Flowable<String>

    @GET(AvMoService.LANGUAGE_NODE + "/popular/pageInfo/{pageInfo}")
    fun getPopular(@Path("pageInfo") page: Int): Flowable<String>

    @GET(AvMoService.LANGUAGE_NODE + "/actresses/pageInfo/{pageInfo}")
    fun getActresses(@Path("pageInfo") page: Int): Flowable<String>

    @GET(AvMoService.LANGUAGE_NODE + "/genre")
    fun genre(): Flowable<String>

    @GET
    operator fun get(@Url url: String): Flowable<String>

    companion object {
        const val LANGUAGE_NODE = "/cn"
        const val URL = "https://avmo.pw"
        val INSTANCE by lazy { NetClient.getRetrofit(URL).create(AvMoService::class.java) }
    }

}
