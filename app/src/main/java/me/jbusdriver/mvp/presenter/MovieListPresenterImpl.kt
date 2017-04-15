package me.jbusdriver.mvp.presenter

import com.cfzx.utils.CacheLoader
import io.reactivex.Flowable
import me.jbusdriver.common.C
import me.jbusdriver.common.KLog
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.MovieListContract
import me.jbusdriver.mvp.MovieListContract.MovieListView
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.model.AbstractBaseModel
import me.jbusdriver.mvp.model.BaseModel
import org.jsoup.nodes.Document

class MovieListPresenterImpl : AbstractRefreshLoadMorePresenterImpl<MovieListView>(), MovieListContract.MovieListPresenter {

    private val service by lazy { mView?.source?.let { JAVBusService.getInstance(it) } ?: JAVBusService.INSTANCE }
    private val loadFromNet = { page: Int ->
        service.getHomePage(page).doOnNext {
            if (page == 1) CacheLoader.lru.put(mView?.source ?: C.Cache.Home, it)
        }
    }

    override val model: BaseModel<Int, String> = object : AbstractBaseModel<Int, String>(loadFromNet) {
        override fun requestFromCache(t: Int): Flowable<String> {
            return Flowable.concat(CacheLoader.justLru(mView?.source ?: C.Cache.Home), requestFor(t)).firstOrError().toFlowable()
        }
    }


    override fun stringMap(str: Document): List<Any> {
        return str.select(".movie-box").map { element ->
            KLog.d(element)
            Movie(title = element.select("img").attr("title"),
                    imageUrl = element.select("img").attr("src"),
                    code = element.select("date").first().text(),
                    date = element.select("date").getOrNull(1)?.text() ?: "",
                    detail = element.attr("href"),
                    tags = element.select(".item-tag").first().children().map { it.text() }
            )
        }
    }

    override fun onRefresh() {
        CacheLoader.removeCacheLike(mView?.source ?: C.Cache.Home, isRegex = false)
        super.onRefresh()
    }

}