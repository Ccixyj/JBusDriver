package me.jbusdriver.mvp.presenter

import io.reactivex.Flowable
import me.jbusdriver.common.SchedulersCompat
import me.jbusdriver.mvp.JapanMovieContract
import me.jbusdriver.mvp.JapanMovieContract.JapanMovieView
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.bean.PageInfo
import me.jbusdriver.mvp.bean.PageItem
import java.util.*
import java.util.concurrent.TimeUnit

class JapanMoviePresenterImpl : AbstractRefreshLoadMorePresenterImpl<JapanMovieView>(), JapanMovieContract.JapanMoviePresenter {

    override fun loadData4Page(page: Int) {
        Flowable.just(page).map { page ->
            pageInfo = PageInfo(PageItem(page), PageItem(page + 1))
            mutableListOf<Movie>().apply {
                val index = (Random().nextInt(8) + 2)
                for (i in 0..index) {
                    add(Movie("${page}_title$i", "", "", "", ""))
                }
            }
        }.delay(1L+Random().nextInt(3),TimeUnit.SECONDS).compose(SchedulersCompat.io())
                .subscribeWith(DefaultSubscriber(page))
    }

}