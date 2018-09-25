package me.jbusdriver.mvp.presenter

import android.support.v4.util.ArrayMap
import io.reactivex.Flowable
import me.jbusdriver.base.*
import me.jbusdriver.base.common.C
import me.jbusdriver.base.CacheLoader
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.bean.PageInfo
import me.jbusdriver.mvp.bean.PageLink
import me.jbusdriver.base.mvp.model.AbstractBaseModel
import me.jbusdriver.base.mvp.model.BaseModel
import me.jbusdriver.ui.data.AppConfiguration
import me.jbusdriver.ui.data.enums.DataSourceType
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * 首页列表
 */
open class HomeMovieListPresenterImpl(val type: DataSourceType, val link: ILink) : LinkAbsPresenterImpl<Movie>(link) {

    private val urls by lazy {
        CacheLoader.acache.getAsString(C.Cache.BUS_URLS)?.let { GSON.fromJson<ArrayMap<String, String>>(it) }
                ?: arrayMapof()
    }
    private val saveKey: String
        inline get() = "${type.key}$IsAll"
    private val service by lazy {
        JAVBusService.getInstance(urls[type.key]
                ?: JAVBusService.defaultFastUrl).apply { JAVBusService.INSTANCE = this }
    }

    private val loadFromNet = { page: Int ->
        val urlN = urls.getOrElse(type.key) { "" }.let { url ->
            return@let if (page == 1) url else "$url${type.prefix}$page"
        }
        KLog.i("load url :$urlN")
        //existmag=all
        //add his
        val pageLink = PageLink(page = page, title = type.key, link = urlN)
        addHistory(pageLink)
        service.get(urlN, if (IsAll) "all" else null).addUserCase().doOnNext {
            if (page == 1 && !it.isNullOrBlank()) CacheLoader.lru.put(saveKey, it!!)
        }.map { Jsoup.parse(it) }.doOnError {
            //可能网址被封
            CacheLoader.acache.remove(C.Cache.BUS_URLS)
        }
    }


    override val model: BaseModel<Int, Document> = object : AbstractBaseModel<Int, Document>(loadFromNet) {
        override fun requestFromCache(t: Int): Flowable<Document> =
                Flowable.concat(CacheLoader.justLru(saveKey).map { Jsoup.parse(it) }, requestFor(t)).firstOrError().toFlowable()
    }


    override fun stringMap(page: PageInfo, str: Document) = Movie.loadFromDoc(str).let {
        when (mView?.pageMode) {
            AppConfiguration.PageMode.Page -> {
                listOf(Movie.newPageMovie(page.activePage, page.referPages)) + it
            }
            else -> it
        }
    }


    override fun onRefresh() {
        CacheLoader.removeCacheLike(saveKey, isRegex = false)
        super.onRefresh()
    }

}