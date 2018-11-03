package debug

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.comp_magnet_activity_main.*
import me.jbusdriver.base.KLog
import me.jbusdriver.component.magnet.R

class CompMagnetMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.comp_magnet_activity_main)
        comp_magnet_tv_go_search.setOnClickListener {
            KLog.d("go search")
        }
    }
}
