package me.jbusdriver.mvp.presenter

import android.util.Base64
import io.reactivex.rxkotlin.addTo
import me.jbusdriver.common.*
import me.jbusdriver.http.RecommendService
import me.jbusdriver.mvp.HotRecommendContract
import me.jbusdriver.mvp.bean.Magnet
import me.jbusdriver.mvp.bean.PageInfo
import me.jbusdriver.mvp.bean.RecommendRespBean
import me.jbusdriver.mvp.bean.ResultPageBean
import me.jbusdriver.mvp.model.BaseModel
import org.jsoup.nodes.Document
import java.util.concurrent.atomic.AtomicInteger

class HotRecommendPresenterImpl : AbstractRefreshLoadMorePresenterImpl<HotRecommendContract.HotRecommendView, Magnet>(), HotRecommendContract.HotRecommendPresenter {

    private val count = AtomicInteger(1)

    override val model: BaseModel<Int, Document>
        get() = error("not call model")

    override fun stringMap(pageInfo: PageInfo, str: Document) = error("not call stringMap")

    override fun onFirstLoad() {
        loadData4Page(count.getAndIncrement())
    }

    override fun loadData4Page(page: Int) {
        KLog.d("loadData4Page :$page")
        RecommendService.INSTANCE.recommends(page)
                .map {
                    val res = it.getAsJsonObject("result")
                    val data = res.getAsJsonArray("data").mapNotNull {
                        it.asString?.let {
                            GSON.fromJson<RecommendRespBean>(String(Base64.decode(it,Base64.DEFAULT or Base64.URL_SAFE)))
                        }
                    }
                    KLog.d(data)
                    val max = res.getAsJsonPrimitive("pages").asInt
                    if (max <= page) {
                        count.set(1)
                    }
                    return@map ResultPageBean(PageInfo(page), data)
                }.doOnTerminate { mView?.dismissLoading() }.compose(SchedulersCompat.io()).subscribe(
                        {
                            KLog.d("$it")
                            mView?.showContents(it.data)
                            mView?.loadMoreEnd()
                            mView?.viewContext?.toast("更换成功！")
                        },{
                    it.printStackTrace()
                    KLog.w(it.message.toString())
                }
                ).addTo(rxManager)

    }

    override fun onLoadMore() {
        loadData4Page(count.getAndIncrement())
    }
    override fun hasLoadNext() = false
}