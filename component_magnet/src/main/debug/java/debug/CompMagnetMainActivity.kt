package debug

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.billy.cc.core.component.CC
import kotlinx.android.synthetic.main.comp_magnet_activity_main.*
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
    }
}
