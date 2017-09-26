package me.jbusdriver.ui.data

import android.content.Context
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.common.AppContext
import me.jbusdriver.common.RxBus
import me.jbusdriver.common.fromJson
import me.jbusdriver.common.toJsonString
import me.jbusdriver.mvp.bean.MenuChangeEvent
import me.jbusdriver.mvp.bean.PageChangeEvent
import kotlin.properties.Delegates

/**
 * Created by Administrator on 2017/9/9.
 */


object AppConfiguration {

    private fun getSp(key: String) = AppContext.instace.getSharedPreferences("config", Context.MODE_PRIVATE).getString(key, null)
    private fun saveSp(key: String, value: String) = Schedulers.io().scheduleDirect { AppContext.instace.getSharedPreferences("config", Context.MODE_PRIVATE).edit().putString(key, value).apply() }

    //region pageMode value
    object PageMode {
        const val Page = 1
        const val Normal = 0
    }

    private const val PageModeS: String = "PageMode"
    var pageMode: Int by Delegates.vetoable(
            getSp(PageModeS)?.toIntOrNull() ?: let {
                saveSp(PageModeS, "1")
                1
            }) { _, old, new ->
        return@vetoable (new in 0..1 && old != new).also {
            if (it) {
                saveSp(PageModeS, new.toString())
                RxBus.post(PageChangeEvent(new))
            }
        }
    }

    //endregion


    //region menu
    private const val MenuConfigS: String = "MenuConfig"

    val menuConfig: MutableMap<String, Boolean> by lazy {
        AppContext.gson.fromJson<MutableMap<String, Boolean>>(
                getSp(MenuConfigS) ?: hashMapOf("最近" to false).toJsonString().apply {
                    saveSp(MenuConfigS, this)
                })
    }

    fun saveSaveMenuConfig(menuOpValue: MutableMap<String, Boolean>) {
        menuConfig.clear()
        menuConfig.putAll(menuOpValue)
        saveSp(MenuConfigS, menuConfig.toJsonString())
        RxBus.post(MenuChangeEvent())
    }
    //endregion

    //region collectCategory

    private const val collectCategoryS: String = "collectCategoryS"
    var enableCategory: Boolean = true

    //endregion

    private const val HistoryS: String = "HistoryS"
    var enableHistory: Boolean = true

}

