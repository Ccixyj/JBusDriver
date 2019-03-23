package debug.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.billy.cc.core.component.CC
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.comp_plugin_manager_activity_main.*
import me.jbusdriver.base.GSON
import me.jbusdriver.base.KLog
import me.jbusdriver.base.common.C
import me.jbusdriver.base.fromJson
import me.jbusdriver.component.plugin.manager.R

class CompPluginManagerActivity : AppCompatActivity() {

    val jsonStr = """
   {
    "internal": [
      {
        "name": "me.jbusdriver.plugin.magnet",
        "versionCode": 3,
        "versionName": "1.0.2",
        "url": "https://raw.githubusercontent.com/Ccixyj/jbusfile/master/plugins/me.jbusdriver.plugin.magnet_1.0.2.apk",
        "desc": "[内部插件]磁力链接解析插件",
        "eTag": "5D9B24BAA625F21D074AA7116BD856CD"
      }
     ]
   }

    """.trimIndent()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.comp_plugin_manager_activity_main)
        val plugins = GSON.fromJson<JsonObject>(jsonStr)
        comp_plugin_manager_tv_install.setOnClickListener {

            KLog.d("comp_plugin_manager_tv_install $plugins")
            CC.obtainBuilder(C.Components.PluginManager)
                .setActionName("plugins.init")
                .setContext(this)
                .addParam("plugins", plugins)
                .build().callAsync()

        }


        comp_plugin_manager_info.setOnClickListener {
            CC.obtainBuilder(C.Components.PluginManager)
                .setActionName("plugins.info")
                .build().callAsync { cc, result ->
                    KLog.d("result ${result.dataMap.values}")
                }
        }
    }

}
