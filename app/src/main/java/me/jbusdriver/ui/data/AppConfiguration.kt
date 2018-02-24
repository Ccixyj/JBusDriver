package me.jbusdriver.ui.data

import android.content.Context
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.common.*
import me.jbusdriver.mvp.bean.CategoryChangeEvent
import me.jbusdriver.mvp.bean.MenuChangeEvent
import me.jbusdriver.mvp.bean.PageChangeEvent
import me.jbusdriver.ui.data.magnet.MagnetLoaders
import kotlin.properties.Delegates


object AppConfiguration {

    private fun getSp(key: String): String? = appContext.getSharedPreferences("config", Context.MODE_PRIVATE).getString(key, null)
    private fun saveSp(key: String, value: String) = Schedulers.io().scheduleDirect { appContext.getSharedPreferences("config", Context.MODE_PRIVATE).edit().putString(key, value).apply() }

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

    //region magnet
    private const val MagnetSourceS: String = "MagnetSourceS"
    val MagnetKeys: MutableList<String> by lazy {
        AppContext.gson.fromJson<MutableList<String>>(getSp(MagnetSourceS) ?: "") ?: let {
            val default = MagnetLoaders.keys.take(2)
            saveSp(MagnetSourceS, default.toJsonString())
            default.toMutableList()
        }
    }

    fun saveMagnetKeys() = saveSp(MagnetSourceS, MagnetKeys.toJsonString())
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
    var enableCategory: Boolean by Delegates.observable(java.lang.Boolean.parseBoolean(getSp(collectCategoryS))) { _, old, new ->
        saveSp(collectCategoryS, new.toString())
        RxBus.post(CategoryChangeEvent())
    }

    //endregion


    private const val HistoryS: String = "HistoryS"
    var enableHistory: Boolean = true




}

