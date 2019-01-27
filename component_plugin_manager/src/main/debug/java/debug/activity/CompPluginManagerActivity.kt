package debug.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.comp_plugin_manager_activity_main.*
import me.jbusdriver.base.KLog
import me.jbusdriver.component.plugin.manager.R

class CompPluginManagerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.comp_plugin_manager_activity_main)
        comp_plugin_manager_tv_install.setOnClickListener {
            KLog.d("comp_plugin_manager_tv_install")
        }
    }

}
