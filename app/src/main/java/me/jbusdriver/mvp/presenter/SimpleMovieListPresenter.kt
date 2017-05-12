package me.jbusdriver.mvp.presenter

import android.net.Uri
import com.cfzx.utils.CacheLoader
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import me.jbusdriver.common.KLog
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
class SimpleMovieListPresenter(val iLink: ILink) : AbstractRefreshLoadMorePresenterImpl<MovieListContract.MovieListView>(), MovieListContract.MovieListPresenter {

    val host by lazy {
        Uri.parse(iLink.link).let {
            checkNotNull(it)
            "${it.scheme}://${it.host}"
        }
    }

    override fun loadAll(iaAll: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /*不需要*/
    override val model: BaseModel<Int, Document> = object : BaseModel<Int, Document> {
        override fun requestFor(t: Int) =
                (if (t == 1) iLink.link else "$host${pageInfo.nextPath}").let {
                    KLog.d("fromCallable page $pageInfo requestFor : $it")
                    JAVBusService.INSTANCE.get(it).map { Jsoup.parse(it) }
                }.doOnNext {
                    if (t == 1) CacheLoader.lru.put(iLink.link, it.toString())
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

    fun parse(link: ILink, doc: Document): IAttr? {
        return when (link) {
            is ActressInfo -> {
                ActressInfo.parseActressAttrs(doc)
            }
            else -> null
        }
    }
}