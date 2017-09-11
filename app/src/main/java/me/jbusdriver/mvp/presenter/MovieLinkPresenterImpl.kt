package me.jbusdriver.mvp.presenter

import io.reactivex.android.schedulers.AndroidSchedulers
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.IAttr
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.ui.data.Configuration
import me.jbusdriver.ui.data.DataSourceType
import org.jsoup.nodes.Document

/**
 * Created by Administrator on 2017/7/29.
 */
class MovieLinkPresenterImpl(val link: ILink) : LinkAbsPresenterImpl<Movie>(link) {

    override fun stringMap(str: Document): List<Movie> {
        //处理ilink
        val iattr = parse(linkData, str)
        iattr?.let {
            AndroidSchedulers.mainThread().scheduleDirect {
                mView?.showContent(it)
            }
        }
        return Movie.loadFromDoc(mView?.type ?: DataSourceType.CENSORED, str).let {
            when (mView?.pageMode) {
                Configuration.PageMode.Page -> {
                    listOf(Movie.newPageMovie(pageInfo.activePage, mView?.type ?: DataSourceType.CENSORED)) + it
                }
                else -> it
            }

        }
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