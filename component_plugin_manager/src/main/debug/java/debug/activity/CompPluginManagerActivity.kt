package debug.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.billy.cc.core.component.CC
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.comp_plugin_manager_activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.rx2.await
import me.jbusdriver.base.GSON
import me.jbusdriver.base.KLog
import me.jbusdriver.base.common.C
import me.jbusdriver.base.fromJson
import me.jbusdriver.common.bean.plugin.PluginBean
import me.jbusdriver.component.plugin.manager.R
import me.jbusdriver.component.plugin.manager.task.DownloadService
import kotlin.coroutines.CoroutineContext

class CompPluginManagerActivity : AppCompatActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.IO + Job()

    private val url = "https://raw.githubusercontent.com/Ccixyj/JBusDriver/%s/api/announce.json"
    private val cache = hashMapOf<String, JsonObject>()

    private val downloadService by lazy { DownloadService.createService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.comp_plugin_manager_activity_main)



        comp_plugin_manager_tv_install.setOnClickListener {
            val dev = url.format("dev")
            launch {
                var cacheObj = cache[dev] ?: JsonObject()
                if ((cacheObj.getAsJsonArray("internal")?.size() ?: 0) <= 0) {
                    val res = downloadService.downloadPluginAsync(dev).await()
                    cacheObj = GSON.fromJson<JsonObject>(res.string()).getAsJsonObject("plugins") ?: JsonObject()
                    cache.put(dev, cacheObj)
                }

                KLog.d("plugins:$cacheObj")
                CC.obtainBuilder(C.Components.PluginManager)
                    .setActionName("plugins.init")
                    .setContext(this@CompPluginManagerActivity)
                    .addParam("plugins", cacheObj)
                    .build().callAsync()
            }


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
        cancel()
    }

}
