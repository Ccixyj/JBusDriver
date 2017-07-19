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
class LinkMovieListPresenterImpl(val iLink: ILink) : AbstractRefreshLoadMorePresenterImpl<MovieListContract.MovieListView>(), MovieListContract.MovieListPresenter {

    var IsAll = false



    override fun loadAll(iaAll: Boolean) {
        IsAll = iaAll
        loadData4Page(1)
    }

    /*不需要*/
    override val model: BaseModel<Int, Document> = object : BaseModel<Int, Document> {
        override fun requestFor(t: Int) =
                (if (t == 1) iLink.link else "${iLink.link.urlHost}${pageInfo.nextPath}").let {
                    KLog.i("fromCallable page $pageInfo requestFor : $it")
                    JAVBusService.INSTANCE.get(it , if (IsAll) "all" else null).map { Jsoup.parse(it) }
                }.doOnNext {
                    if (t == 1) CacheLoader.lru.put("${iLink.link}$IsAll", it.toString())
                }

        override fun requestFromCache(t: Int) = Flowable.concat(CacheLoader.justLru(iLink.link).map { Jsoup.parse(it) }, requestFor(t))
                .firstOrError().toFlowable()
    }


    override fun stringMap(str: Document): List<Any> {

        //处理ilink
        val iattr = parse(iLink, str)
        iattr?.let {
            AndroidSchedulers.mainThread().scheduleDirect {
                mView?.showContent(it)
            }
        }

        return Movie.loadFromDoc(mView?.type ?: DataSourceType.CENSORED, str)
    }

    override fun onRefresh() {
        CacheLoader.lru.remove(iLink.link)
        super.onRefresh()
    }

    private  fun parse(link: ILink, doc: Document): IAttr? {
        return when (link) {
            is ActressInfo -> {
                ActressInfo.parseActressAttrs(doc)
            }
            else -> null
        }
    }
}