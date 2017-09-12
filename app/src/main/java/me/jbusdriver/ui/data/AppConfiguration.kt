package me.jbusdriver.ui.data

import android.content.Context
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.common.AppContext
import me.jbusdriver.common.RxBus
import me.jbusdriver.common.fromJson
import me.jbusdriver.common.toJsonString
import me.jbusdriver.mvp.bean.PageChangeEvent
import kotlin.properties.Delegates

/**
 * Created by Administrator on 2017/9/9.
 */


object AppConfiguration {

    private fun getSp(key: String) = AppContext.instace.getSharedPreferences("config", Context.MODE_PRIVATE).getString(key, null)
    private fun saveSp(key: String, value: String) = AppContext.instace.getSharedPreferences("config", Context.MODE_PRIVATE).edit().putString(key, value).apply()

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
    private const val MenuConfigS: String = "MenuConfig"

    var menuConfig: Map<Int, Boolean> by Delegates.observable(AppContext.gson.fromJson<Map<Int, Boolean>>(
            getSp(MenuConfigS) ?: mapOf(R.id.mine_history to false).toJsonString().apply {
                saveSp(MenuConfigS, this)
            })) { _, old, new ->
        saveSp(MenuConfigS, new.toJsonString())
    }

}

