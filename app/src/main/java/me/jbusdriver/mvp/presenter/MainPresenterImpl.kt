package me.jbusdriver.mvp.presenter

import me.jbusdriver.mvp.MainContract

class MainPresenterImpl : BasePresenterImpl<MainContract.MainView>(), MainContract.MainPresenter{
    override fun onFirstLoad() {
        super.onFirstLoad()
        fetchUpdate()
    }

    private fun fetchUpdate() {
//        Flowable.concat<UpdateBean>( CacheLoader.justLru(C.Cache.ANNOUNCE_VALUE).map { AppContext.gson.fromJson<UpdateBean>(it) },
//
//                )
    }
}