package me.jbusdriver.mvp.presenter

import me.jbusdriver.base.mvp.bean.PageInfo
import me.jbusdriver.base.mvp.model.BaseModel
import me.jbusdriver.base.mvp.presenter.AbstractRefreshLoadMorePresenterImpl
import me.jbusdriver.common.bean.ILink
import me.jbusdriver.mvp.HotRecommendContract
import org.jsoup.nodes.Document
import java.util.concurrent.atomic.AtomicInteger

class HotRecommendPresenterImpl : AbstractRefreshLoadMorePresenterImpl<HotRecommendContract.HotRecommendView, ILink>(), HotRecommendContract.HotRecommendPresenter {

    private val count = AtomicInteger(1)

    override val model: BaseModel<Int, Document>
        get() = error("not call model")

    override fun stringMap(pageInfo: PageInfo, str: Document) = error("not call stringMap")

    override fun onFirstLoad() {
        loadData4Page(count.get())
    }

    override fun loadData4Page(page: Int) {
/*
        RecommendService.INSTANCE.recommends(page)
                .map {
                    val res = it.getAsJsonObject("result")
                    val data = res.getAsJsonArray("data").mapNotNull {
                        it.asString?.let {
                            val bean = GSON.fromJson<RecommendRespBean>(String(Base64.decode(it, Base64.DEFAULT or Base64.URL_SAFE)))
                            if (!Uri.parse(bean.key.url).isAbsolute) {
                                val images = JAVBusService.defaultImageUrlHosts.flatMap { it.value }.map {
                                    mView?.viewContext?.let { c ->
                                        Glide.with(c).load((it + bean.key.img).toGlideNoHostUrl).submit()
                                    }
                                    it + bean.key.img
                                }.shuffled()
                                bean.copy(key = bean.key.copy(img = images.firstOrNull()
                                        ?: bean.key.img, url = JAVBusService.defaultFastUrl + bean.key.url))
                            } else bean
                        }
                    }
                    val max = res.getAsJsonPrimitive("pages").asInt
                    lastPage = max
                    return@map ResultPageBean(PageInfo(page), data)
                }.doOnTerminate { mView?.dismissLoading() }.compose(SchedulersCompat.io()).subscribe(
                        {
                            mView?.showContents(it.data)
                            mView?.loadMoreEnd()
                            mView?.viewContext?.toast("加载成功！")

                        }, {
                    it.printStackTrace()
                }
                ).addTo(rxManager)
*/

    }

    override fun onLoadMore() {
        if (lastPage <  count.incrementAndGet()) {
            count.set(1)
        }
        loadData4Page(count.get())
    }

    override fun hasLoadNext() = false
    override fun onRefresh() {
        if (lastPage < count.get()) {
            count.set(1)
        }
        rxManager.clear()
        loadData4Page(count.get())
    }
}