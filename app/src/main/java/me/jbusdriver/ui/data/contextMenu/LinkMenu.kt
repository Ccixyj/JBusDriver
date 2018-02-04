package me.jbusdriver.ui.data.contextMenu

import me.jbusdriver.common.AppContext
import me.jbusdriver.common.KLog
import me.jbusdriver.common.copy
import me.jbusdriver.common.toast
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.ILink
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.bean.des
import me.jbusdriver.ui.data.collect.ActressCollector
import me.jbusdriver.ui.data.collect.LinkCollector
import me.jbusdriver.ui.data.collect.MovieCollector

/**
 * Created by Administrator on 2018/2/4.
 */
object LinkMenu {

    val movieActions by lazy {
        mapOf("复制标题" to { movie: Movie ->
            AppContext.instace.copy(movie.title)
            AppContext.instace.toast("已复制")
        }, "复制番号" to { movie: Movie ->
            AppContext.instace.copy(movie.code)
            AppContext.instace.toast("已复制")
        }, "收藏" to { movie: Movie ->
            MovieCollector.addToCollect(movie)
            KLog.d("actress_data:${ActressCollector.dataList}")
        }, "取消收藏" to { movie: Movie ->
            MovieCollector.removeCollect(movie)
            KLog.d("actress_data:${ActressCollector.dataList}")
        })
    }


    val actressActions by lazy {
        mapOf("复制名字" to { act: ActressInfo ->
            AppContext.instace.copy(act.name)
            AppContext.instace.toast("已复制")
        }, "收藏" to { act: ActressInfo ->
            ActressCollector.addToCollect(act)
            KLog.d("actress_data:${ActressCollector.dataList}")
        }, "取消收藏" to { act: ActressInfo ->
            ActressCollector.removeCollect(act)
            KLog.d("actress_data:${ActressCollector.dataList}")
        })

    }


    val linkActions by lazy {
        mapOf("复制" to { link: ILink ->
            KLog.d("copy $link ${link.des}")
            AppContext.instace.copy(link.des.split(" ").last())
            AppContext.instace.toast("已复制")
        }, "收藏" to { link ->
            LinkCollector.addToCollect(link)
            KLog.d("link data ${LinkCollector.dataList}")
        }, "取消收藏" to { link ->
            LinkCollector.removeCollect(link)
            KLog.d("link data ${LinkCollector.dataList}")
        })
    }

}