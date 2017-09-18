package me.jbusdriver.mvp.presenter

import io.reactivex.android.schedulers.AndroidSchedulers
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.ui.data.AppConfiguration
import me.jbusdriver.ui.data.DataSourceType
import org.jsoup.nodes.Document

/**
 * 网页链接列表
 */
class MovieLinkPresenterImpl(val link: ILink) : LinkAbsPresenterImpl<Movie>(link) {

    override fun stringMap(str: Document): List<Movie> {
        //处理alert
        parseMoviesAlert(str).let {
            AndroidSchedulers.mainThread().scheduleDirect {
                mView?.showContent(it)
            }
        }

        //处理ilink
        parseActressAttrs(linkData, str)?.let {
            AndroidSchedulers.mainThread().scheduleDirect {
                mView?.showContent(it)
            }
        }

        return Movie.loadFromDoc(mView?.type ?: DataSourceType.CENSORED, str).let {
            when (mView?.pageMode) {
                AppConfiguration.PageMode.Page -> {
                    listOf(Movie.newPageMovie(pageInfo.activePage, pageInfo.pages, mView?.type ?: DataSourceType.CENSORED)) + it
                }
                else -> it
            }

        }
    }


    private fun parseActressAttrs(link: ILink, doc: Document) = when (link) {
        is ActressInfo -> {
            ActressInfo.parseActressAttrs(doc)
        }
        else -> null
    }

    private fun parseMoviesAlert(str: Document): String {
        return str.select(".alert-success").text().let {
            it.filter { !it.isWhitespace() }
        }
    }

}