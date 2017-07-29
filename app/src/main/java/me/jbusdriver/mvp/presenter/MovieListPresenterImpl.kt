package me.jbusdriver.mvp.presenter

import android.support.v4.util.ArrayMap
import com.cfzx.utils.CacheLoader
import io.reactivex.Flowable
import me.jbusdriver.common.*
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.LinkListContract
import me.jbusdriver.mvp.LinkListContract.LinkListView
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.model.AbstractBaseModel
import me.jbusdriver.mvp.model.BaseModel
import me.jbusdriver.ui.data.DataSourceType
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

open class MovieListPresenterImpl : AbstractRefreshLoadMorePresenterImpl<LinkListView,Movie>(), LinkListContract.LinkListPresenter {

    private  var IsAll = false
    private val urls by lazy { CacheLoader.acache.getAsString(C.Cache.BUS_URLS)?.let { AppContext.gson.fromJson<ArrayMap<String, String>>(it) } ?: arrayMapof() }
    private val saveKey: String
        inline get() = "${mView?.type?.key ?: DataSourceType.CENSORED.key}$IsAll"
    private val service by lazy {
        mView?.let {
            JAVBusService.getInstance(urls.get(it.type.key) ?: JAVBusService.defaultFastUrl).apply { JAVBusService.INSTANCE = this }
        } ?: JAVBusService.INSTANCE
    }
    private val loadFromNet = { page: Int ->
        val type = mView?.type ?: DataSourceType.CENSORED
        val urlN = (urls.get(type.key) ?: "").let {
            url ->
            return@let if (page == 1) url else "$url${type.prefix}$page"
        }
        KLog.i("load url :$urlN")
        //existmag=all
        service.get(urlN, if (IsAll) "all" else null) .doOnNext {
            if (page == 1) CacheLoader.lru.put(saveKey, it)
        }.map { Jsoup.parse(it) }
    }

    override val model: BaseModel<Int, Document> = object : AbstractBaseModel<Int, Document>(loadFromNet) {
        override fun requestFromCache(t: Int): Flowable<Document> {
            return Flowable.concat(CacheLoader.justLru(saveKey).map { Jsoup.parse(it) }, requestFor(t)).firstOrError().toFlowable()
        }
    }


    override fun stringMap(str: Document) = Movie.loadFromDoc(mView?.type ?: DataSourceType.CENSORED, str)

    override fun onRefresh() {
        CacheLoader.removeCacheLike(saveKey, isRegex = false)
        super.onRefresh()
    }

    override fun loadAll(iaAll: Boolean) {
        IsAll = iaAll
        loadData4Page(1)
    }
}