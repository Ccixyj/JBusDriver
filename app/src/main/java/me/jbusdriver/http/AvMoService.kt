package me.jbusdriver.http

import io.reactivex.Flowable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

/**
 * Project: JAViewer
 */
interface AvMoService {

    @GET(AvMoService.LANGUAGE_NODE + "/page/{page}")
    fun getHomePage(@Path("page") page: Int): Flowable<String>

    @GET(AvMoService.LANGUAGE_NODE + "/released/page/{page}")
    fun getReleased(@Path("page") page: Int): Flowable<String>

    @GET(AvMoService.LANGUAGE_NODE + "/popular/page/{page}")
    fun getPopular(@Path("page") page: Int): Flowable<String>

    @GET(AvMoService.LANGUAGE_NODE + "/actresses/page/{page}")
    fun getActresses(@Path("page") page: Int): Flowable<String>

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
