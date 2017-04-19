package me.jbusdriver.mvp.presenter

import android.support.v4.util.ArrayMap
import com.cfzx.utils.CacheLoader
import io.reactivex.Flowable
import me.jbusdriver.common.AppContext
import me.jbusdriver.common.C
import me.jbusdriver.common.arrayMapof
import me.jbusdriver.common.fromJson
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.MovieListContract
import me.jbusdriver.mvp.MovieListContract.MovieListView
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.model.AbstractBaseModel
import me.jbusdriver.mvp.model.BaseModel
import me.jbusdriver.ui.data.DataSourceType
import org.jsoup.nodes.Document

class MovieListPresenterImpl : AbstractRefreshLoadMorePresenterImpl<MovieListView>(), MovieListContract.MovieListPresenter {

    var IsAll = false
    val saveKey: String
        inline get() = "${mView?.type?.key ?: DataSourceType.CENSORED.key}$IsAll"
    val urls by lazy { CacheLoader.acache.getAsString(C.Cache.BUS_URLS)?.let { AppContext.gson.fromJson<ArrayMap<String, String>>(it) } ?: arrayMapof() }
    private val service by lazy {
        mView?.let {
            JAVBusService.getInstance(urls.get(it.type.key) ?: JAVBusService.defaultFastUrl).apply { JAVBusService.INSTANCE = this }
        } ?: JAVBusService.INSTANCE
    }
    private val loadFromNet = { page: Int ->
        service.getHomePage(page, if (IsAll) "all" else null).doOnNext {
            if (page == 1) CacheLoader.lru.put(saveKey, it)
        }
    }

    override val model: BaseModel<Int, String> = object : AbstractBaseModel<Int, String>(loadFromNet) {
        override fun requestFromCache(t: Int): Flowable<String> {
            return Flowable.concat(CacheLoader.justLru(saveKey), requestFor(t)).firstOrError().toFlowable()
        }
    }


    override fun stringMap(str: Document): List<Any> = Movie.loadFromDoc(mView?.type ?: DataSourceType.CENSORED ,str)

    override fun onRefresh() {
        CacheLoader.removeCacheLike(saveKey, isRegex = false)
        super.onRefresh()
    }

    override fun loadAll(iaAll: Boolean) {
        IsAll = iaAll
        loadData4Page(1)
    }
}