package me.jbusdriver.component.magnet.ui.activity

import android.os.Bundle
import com.billy.cc.core.component.CC
import com.billy.cc.core.component.CCUtil
import kotlinx.android.synthetic.main.comp_magnet_activity_magnet_list.*
import me.jbusdriver.base.JBusManager.context
import me.jbusdriver.base.common.BaseActivity
import me.jbusdriver.base.common.C
import me.jbusdriver.component.magnet.R
import me.jbusdriver.component.magnet.ui.fragment.MagnetPagersFragment

class MagnetPagerListActivity : BaseActivity() {

    private val keyword by lazy {
        intent.getStringExtra(C.BundleKey.Key_1) ?: error("must set keyword")
    }
    private val link by lazy { intent.getStringExtra(C.BundleKey.Key_2).orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.comp_magnet_activity_magnet_list)
        setSupportActionBar(comp_magnet_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(keyword)
        //go to SearchResultPagesFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.comp_magnet_fl_magnet_list, MagnetPagersFragment().apply {
                arguments = Bundle().apply {
                    putString(C.BundleKey.Key_1, keyword)
                    putString(C.BundleKey.Key_2, link)
                }
            }).commit()

    }

    private fun setTitle(title: String) {
        supportActionBar?.title = "$title 的磁力链接"
    }

    companion object {
        fun start(cc: CC, keyword: String, link: String) {
            context.startActivity(
                CCUtil.createNavigateIntent(
                    cc,
                    MagnetPagerListActivity::class.java
                ).apply {
                    putExtra(C.BundleKey.Key_1, keyword)
                    putExtra(C.BundleKey.Key_2, link)
                })
        }
    }
}
