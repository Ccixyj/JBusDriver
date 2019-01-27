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
        "versionCode": 2,
        "versionName": "1.0.0",
        "tag": "plugin.magnet",
        "url": "https://github.com/Ccixyj/jbusfile/blob/master/plugins/me.jbusdriver.plugin.magnet_1.0.0.apk?raw=true",
        "desc": "[内部插件]磁力链接解析插件",
        "eTag": "FE950EAAEF74590B8FC59B354F27EB9F"
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
                    .addParam("plugins", plugins)
                    .build().callAsync()

        }
    }

}
