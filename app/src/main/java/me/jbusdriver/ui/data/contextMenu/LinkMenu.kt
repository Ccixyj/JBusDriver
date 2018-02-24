package me.jbusdriver.ui.data.contextMenu

import me.jbusdriver.common.KLog
import me.jbusdriver.common.appContext
import me.jbusdriver.common.copy
import me.jbusdriver.common.toast
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.mvp.model.CollectModel


object LinkMenu {

    val movieActions by lazy {
        mapOf("复制标题" to { movie: Movie ->
            appContext.copy(movie.title)
            appContext.toast("已复制")
        }, "复制番号" to { movie: Movie ->
            appContext.copy(movie.code)
            appContext.toast("已复制")
        }, "收藏" to { movie: Movie ->
            CollectModel.addToCollect(movie.convertDBItem())
        }, "取消收藏" to { movie: Movie ->
            CollectModel.removeCollect(movie.convertDBItem())
        })
    }


    val actressActions by lazy {
        mapOf("复制名字" to { act: ActressInfo ->
            appContext.copy(act.name)
            appContext.toast("已复制")
        }, "收藏" to { act: ActressInfo ->
            CollectModel.addToCollect(act.convertDBItem())
        }, "取消收藏" to { act: ActressInfo ->
            CollectModel.removeCollect(act.convertDBItem())
        })

    }


    val linkActions by lazy {
        mapOf("复制" to { link: ILink ->
            KLog.d("copy $link ${link.des}")
            appContext.copy(link.des.split(" ").last())
            appContext.toast("已复制")
        }, "收藏" to { link ->
            CollectModel.addToCollect(link.convertDBItem())
        }, "取消收藏" to { link ->
            CollectModel.removeCollect(link.convertDBItem())
        })
    }

}