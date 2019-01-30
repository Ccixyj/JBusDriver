package me.jbusdriver.ui.data.contextMenu

import me.jbusdriver.base.copy
import me.jbusdriver.base.toast
import me.jbusdriver.common.JBus
import me.jbusdriver.common.bean.ILink
import me.jbusdriver.mvp.bean.ActressInfo
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.bean.convertDBItem
import me.jbusdriver.mvp.bean.des
import me.jbusdriver.mvp.model.CollectModel

/**
 * Created by Administrator on 2018/2/4.
 */
object LinkMenu {

    val movieActions by lazy {
        mapOf("复制标题" to { movie: Movie ->
            JBus.copy(movie.title)
            toast("已复制")
        }, "复制番号" to { movie: Movie ->
            JBus.copy(movie.code)
            toast("已复制")
        }, "收藏" to { movie: Movie ->
            CollectModel.addToCollectForCategory(movie.convertDBItem())
        }, "取消收藏" to { movie: Movie ->
            CollectModel.removeCollect(movie.convertDBItem())
        })
    }


    val actressActions by lazy {
        mapOf("复制名字" to { act: ActressInfo ->
            JBus.copy(act.name)
            toast("已复制")
        }, "收藏" to { act: ActressInfo ->
            CollectModel.addToCollectForCategory(act.convertDBItem())
        }, "取消收藏" to { act: ActressInfo ->
            CollectModel.removeCollect(act.convertDBItem())
        })

    }


    val linkActions by lazy {
        mapOf("复制" to { link: ILink ->
            JBus.copy(link.des.substring(link.des.indexOf(" ").coerceAtLeast(0)))
            toast("已复制")
        }, "收藏" to { link ->
            CollectModel.addToCollectForCategory(link.convertDBItem())
        }, "取消收藏" to { link ->
            CollectModel.removeCollect(link.convertDBItem())
        })
    }


}