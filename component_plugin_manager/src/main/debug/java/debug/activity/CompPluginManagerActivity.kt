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
import me.jbusdriver.component.plugin.manager.PluginManagerComponent
import me.jbusdriver.component.plugin.manager.R

class CompPluginManagerActivity : AppCompatActivity() {

    val jsonStr = """
   {
    "internal": [
      {
        "name": "me.jbusdriver.plugin.magnet",
        "versionCode": 2,
        "versionName": "1.0.0",
        "url": "https://github.com/Ccixyj/jbusfile/blob/master/plugins/me.jbusdriver.plugin.magnet_1.0.0.apk?raw=true",
        "desc": "[内部插件]磁力链接解析插件",
        "eTag": "FE950EAAEF74590B8FC59B354F27EB9F"
      },
      {
        "name": "me.jbusdriver.plugin.magnet1",
        "versionCode": 2,
        "versionName": "1.0.1",
        "url": "http://192.168.1.179:8080/static/1.apk",
        "desc": "[内部插件]磁力链接解析插件2",
        "eTag": "A83ED9A177FE7BF3885C0D27E703C489"
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
    }

}
