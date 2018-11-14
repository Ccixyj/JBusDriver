package debug

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.billy.cc.core.component.CC
import kotlinx.android.synthetic.main.comp_magnet_activity_main.*
import me.jbusdriver.base.KLog
import me.jbusdriver.base.common.C
import me.jbusdriver.component.magnet.R

class CompMagnetMainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.comp_magnet_activity_main)
        comp_magnet_tv_go_search.setOnClickListener {
            CC.obtainBuilder(C.Components.Manget)
                    .setActionName("show")
                    .addParam("keyword", et_keyword.text.toString())
                    .build().call()
        }

        //"allKeys" , "config.save" , "config.getKeys"
        comp_magnet_tv_get_all.setOnClickListener {
            CC.obtainBuilder(C.Components.Manget)
                    .setActionName("allKeys")
                    .build().call()
        }

        comp_magnet_tv_config_get.setOnClickListener {
            CC.obtainBuilder(C.Components.Manget)
                    .setActionName("config.getKeys")
                    .build().call()
        }

        comp_magnet_tv_config_save.setOnClickListener {
            val res = CC.obtainBuilder(C.Components.Manget)
                    .setActionName("allKeys")
                    .build().call()
            if (res.isSuccess) {
                val allKeys = res.getDataItem<List<String>>("keys")
                CC.obtainBuilder(C.Components.Manget)
                        .setActionName("config.save")
                        .addParam("keys", allKeys.shuffled().take((Math.random() * allKeys.size).toInt().coerceIn(1..allKeys.size)))
                        .build().call()
            } else {
                KLog.w("error get all key : ${res.errorMessage}")
            }

        }
    }
}
