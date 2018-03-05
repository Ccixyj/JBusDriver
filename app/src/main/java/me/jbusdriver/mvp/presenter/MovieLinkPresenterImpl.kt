package me.jbusdriver.mvp.presenter

import io.reactivex.schedulers.Schedulers
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.mvp.model.CollectModel
import me.jbusdriver.ui.data.AppConfiguration
import org.jsoup.nodes.Document

/**
 * 网页链接列表
 */
class MovieLinkPresenterImpl(val link: ILink, isAllFromBundle: Boolean, isHis: Boolean) : LinkAbsPresenterImpl<Movie>(link, isHis) {

    override var IsAll = isAllFromBundle

    override fun stringMap(page: PageInfo, str: Document): List<Movie> {
        //处理alert
        parseMoviesAlert(str).let {
            mView?.showContent(it)
        }

        //处理ilink : actress collect
        parseAttrs(linkData, str)?.let {
            mView?.showContent(it)
        }

        return Movie.loadFromDoc( str).let {
            when (mView?.pageMode) {
                AppConfiguration.PageMode.Page -> {
                    listOf(Movie.newPageMovie(pageInfo.activePage, pageInfo.referPages)) + it
                }
                else -> it
            }

        }
    }


    private fun parseAttrs(link: ILink, doc: Document) = when (link) {
        is ActressInfo -> {
            ActressInfo.parseActressAttrs(doc).let { attr ->
                Schedulers.single().scheduleDirect {
                    if (link.avatar != attr.imageUrl) {
                        //如果已收藏演员, 需要重新设置头像
                        CollectModel.update(link.copy(avatar = attr.imageUrl).convertDBItem())
                    }
                }
                attr
            }
        }
        else -> null
    }

    private fun parseMoviesAlert(str: Document): String {
        return str.select(".alert-success").text().let {
            it.filter { !it.isWhitespace() }
        }
    }

}