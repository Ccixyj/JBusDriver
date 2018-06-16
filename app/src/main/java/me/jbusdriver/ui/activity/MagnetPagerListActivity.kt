package me.jbusdriver.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.activity_search_result.*
import me.jbusdriver.base.common.BaseActivity
import me.jbusdriver.base.common.C
import me.jbusdriver.ui.fragment.MagnetPagersFragment

class MagnetPagerListActivity : BaseActivity() {

    private val keyword by lazy { intent.getStringExtra(C.BundleKey.Key_1) ?: error("must set keyword") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_magnet_list)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(keyword)
        //go to SearchResultPagesFragment
        supportFragmentManager.beginTransaction().replace(R.id.fl_magnet_list, MagnetPagersFragment().apply {
            arguments = Bundle().apply { putString(C.BundleKey.Key_1, keyword) }
        }).commit()

    }

    private fun setTitle(title: String) {
        supportActionBar?.title = "$title 的磁力链接"
    }

    companion object {
        fun start(context: Context, keyword: String) {
            context.startActivity(Intent(context, MagnetPagerListActivity::class.java).apply {
                putExtra(C.BundleKey.Key_1, keyword)
            })
        }
    }
}
