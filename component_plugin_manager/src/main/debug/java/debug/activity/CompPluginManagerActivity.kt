package debug.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.billy.cc.core.component.CC
import com.google.gson.JsonObject
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.comp_plugin_manager_activity_main.*
import me.jbusdriver.base.GSON
import me.jbusdriver.base.KLog
import me.jbusdriver.base.SchedulersCompat
import me.jbusdriver.base.common.C
import me.jbusdriver.base.fromJson
import me.jbusdriver.common.bean.plugin.PluginBean
import me.jbusdriver.component.plugin.manager.R
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class CompPluginManagerActivity : AppCompatActivity() {

    private val rxManager = CompositeDisposable()

    private val url = "https://raw.githubusercontent.com/Ccixyj/JBusDriver/%s/api/announce.json"
    private val cache = hashMapOf<String, JsonObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.comp_plugin_manager_activity_main)

        val dev = url.format("dev")

        if ((cache[dev]?.getAsJsonArray("internal")?.size() ?: 0) <= 0) {
            Flowable.just(dev)
                .map {
                    OkHttpClient().newCall(Request.Builder().url(it).get().build())
                        .execute().body()?.string() ?: error("request error")
                }
                .map {
                    GSON.fromJson<JsonObject>(it).getAsJsonObject("plugins") ?: JsonObject()
                }
                .compose(SchedulersCompat.io())
                .timeout(30, TimeUnit.SECONDS)
                .subscribeBy {
                    cache.put(dev, it)
                    KLog.d("resolved : $it")
                }
                .addTo(rxManager)
        }

        comp_plugin_manager_tv_install.setOnClickListener {

            val plugins = cache[dev] ?: return@setOnClickListener
            CC.obtainBuilder(C.Components.PluginManager)
                .setActionName("plugins.init")
                .setContext(this)
                .addParam("plugins", plugins)
                .build().callAsync()

        }


        comp_plugin_manager_info.setOnClickListener {
            KLog.d("check comp manager : ${C.Components.PluginManager} : ${CC.hasComponent(C.Components.PluginManager)}")

            CC.obtainBuilder(C.Components.PluginManager)
                .setActionName("plugins.info")
                .build().callAsyncCallbackOnMainThread { _, result ->
                    val plugins = result?.getDataItemWithNoKey() as? List<PluginBean> ?: emptyList()
                    KLog.d("result ${result?.dataMap?.values} ==> $plugins")
                    comp_plugin_manager_info_content.text = plugins.joinToString("\r\n==============\r\n")
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rxManager.clear()
    }

}
