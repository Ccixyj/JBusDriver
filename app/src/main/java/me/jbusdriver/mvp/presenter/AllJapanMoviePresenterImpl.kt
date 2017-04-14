package me.jbusdriver.mvp.presenter

import com.cfzx.utils.CacheLoader
import io.reactivex.Flowable
import me.jbusdriver.common.C
import me.jbusdriver.common.KLog
import me.jbusdriver.mvp.AllJapanMovieContract
import me.jbusdriver.mvp.AllJapanMovieContract.AllJapanMovieView
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.model.AbstractBaseModel
import me.jbusdriver.mvp.model.BaseModel
import org.jsoup.nodes.Document

class AllJapanMoviePresenterImpl : AbstractRefreshLoadMorePresenterImpl<AllJapanMovieView>(), AllJapanMovieContract.AllJapanMoviePresenter {


    override val model: BaseModel<Int, String> = object : AbstractBaseModel<Int, String>({ me.jbusdriver.http.JAVBusService.INSTANCE.getHomePage(it) }) {
        override fun requestFromCache(t: Int): Flowable<String> {
            return Flowable.concat(CacheLoader.fromLruAsync(C.Cache.Home), requestFor(t)).firstOrError().toFlowable()
        }
    }


    override fun stringMap(str: Document): List<Any> {
        return str.select(".movie-box").map {  element ->
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
        CacheLoader.removeCacheLike(C.Cache.Home,isRegex = false)
        super.onRefresh()
    }

}