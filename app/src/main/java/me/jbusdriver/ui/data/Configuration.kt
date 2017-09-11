package me.jbusdriver.ui.data

import android.content.Context
import com.google.gson.JsonObject
import me.jbusdriver.common.AppContext
import me.jbusdriver.common.RxBus
import me.jbusdriver.common.fromJson
import kotlin.properties.Delegates

/**
 * Created by Administrator on 2017/9/9.
 */


object Configuration {
    private val _configData: JsonObject by lazy {
        AppContext.gson.fromJson<JsonObject>(getSp()) ?: init()
    }

    private fun init() = JsonObject().apply {
        addProperty(PageModeS, 1)
        AppContext.instace.getSharedPreferences("config", Context.MODE_PRIVATE).edit().putString("app_config", this.toString()).apply()
    }

    private fun getSp() = AppContext.instace.getSharedPreferences("config", Context.MODE_PRIVATE).getString("app_config", "")
    private fun saveSp() = AppContext.instace.getSharedPreferences("config", Context.MODE_PRIVATE).edit().putString("app_config", _configData.toString()).apply()

    //region pageMode value
    object PageMode {
        const val Page = 1
        const val Normal = 0
    }

    private const val PageModeS: String = "PageMode"
    var pageMode: Int by Delegates.vetoable(_configData.get(PageModeS).asInt) { _, old, new ->
        return@vetoable (new in 0..1 && old != new).also {
            if (it) {
                _configData.addProperty(PageModeS, new)
                saveSp()
                RxBus.post(PageChangeEvent(new))
            }
        }
    }

    /*value invoke*/
    data class PageChangeEvent(val mode: Int)
    /*add listener*/


    //endregion

}

