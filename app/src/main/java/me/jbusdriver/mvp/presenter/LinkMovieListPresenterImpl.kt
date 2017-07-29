package me.jbusdriver.mvp.presenter

import com.cfzx.utils.CacheLoader
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import me.jbusdriver.common.KLog
import me.jbusdriver.common.urlHost
import me.jbusdriver.http.JAVBusService
import me.jbusdriver.mvp.MovieListContract
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.IAttr
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.model.BaseModel
import me.jbusdriver.ui.data.DataSourceType
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Created by Administrator on 2017/5/10 0010.
 */
class LinkMovieListPresenterImpl(val linkData: ILink) : AbstractRefreshLoadMorePresenterImpl<MovieListContract.MovieListView>(), MovieListContract.MovieListPresenter {

    private var IsAll = false

    override fun loadAll(iaAll: Boolean) {
        IsAll = iaAll
        loadData4Page(1)
    }

    /*不需要*/
    override val model: BaseModel<Int, Document> = object : BaseModel<Int, Document> {
        override fun requestFor(t: Int) =
                (if (t == 1) linkData.link else "${linkData.link.urlHost}${pageInfo.nextPath}").let {
                    KLog.i("fromCallable page $pageInfo requestFor : $it")
                    JAVBusService.INSTANCE.get(it, if (IsAll) "all" else null).map { Jsoup.parse(it) }
                }.doOnNext {
                    if (t == 1) CacheLoader.lru.put("${linkData.link}$IsAll", it.toString())
                }

        override fun requestFromCache(t: Int) = Flowable.concat(CacheLoader.justLru(linkData.link).map { Jsoup.parse(it) }, requestFor(t))
                .firstOrError().toFlowable()
    }


    override fun stringMap(str: Document): List<Any> {

        //处理ilink
        val iattr = parse(linkData, str)
        iattr?.let {
            AndroidSchedulers.mainThread().scheduleDirect {
                mView?.showContent(it)
            }
        }

        return Movie.loadFromDoc(mView?.type ?: DataSourceType.CENSORED, str)
    }

    override fun onRefresh() {
        CacheLoader.lru.remove(linkData.link)

        super.onRefresh()
    }

    private fun parse(link: ILink, doc: Document): IAttr? {
        return when (link) {
            is ActressInfo -> {
                ActressInfo.parseActressAttrs(doc)
            }
            else -> null
        }
    }
}