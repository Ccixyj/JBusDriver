package me.jbusdriver.ui.data.contextMenu

import me.jbusdriver.common.JBus
import me.jbusdriver.common.KLog
import me.jbusdriver.common.copy
import me.jbusdriver.common.toast
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.mvp.model.CollectModel

/**
 * Created by Administrator on 2018/2/4.
 */
object LinkMenu {

    val movieActions by lazy {
        mapOf("复制标题" to { movie: Movie ->
            JBus.copy(movie.title)
            JBus.toast("已复制")
        }, "复制番号" to { movie: Movie ->
            JBus.copy(movie.code)
            JBus.toast("已复制")
        }, "收藏" to { movie: Movie ->
            CollectModel.addToCollect(movie.convertDBItem())
        }, "取消收藏" to { movie: Movie ->
            CollectModel.removeCollect(movie.convertDBItem())
        })
    }


    val actressActions by lazy {
        mapOf("复制名字" to { act: ActressInfo ->
            JBus.copy(act.name)
            JBus.toast("已复制")
        }, "收藏" to { act: ActressInfo ->
            CollectModel.addToCollect(act.convertDBItem())
        }, "取消收藏" to { act: ActressInfo ->
            CollectModel.removeCollect(act.convertDBItem())
        })

    }


    val linkActions by lazy {
        mapOf("复制" to { link: ILink ->
            KLog.d("copy $link ${link.des}")
            JBus.copy(link.des.split(" ").last())
            JBus.toast("已复制")
        }, "收藏" to { link ->
            CollectModel.addToCollect(link.convertDBItem())
        }, "取消收藏" to { link ->
            CollectModel.removeCollect(link.convertDBItem())
        })
    }

}