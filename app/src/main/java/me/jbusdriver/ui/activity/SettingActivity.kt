package me.jbusdriver.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.activity_setting.*
import me.jbusdriver.common.BaseActivity
import me.jbusdriver.ui.data.Configuration

class SettingActivity : BaseActivity() {

    private var pageModeHolder = Configuration.pageMode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setToolBar()
        initSettingView()
    }

    private fun initSettingView() {
        //page mode
        changePageMode(Configuration.pageMode)
        ll_page_mode_page.setOnClickListener {
            pageModeHolder = Configuration.PageMode.Page
            changePageMode(Configuration.PageMode.Page)
        }
        ll_page_mode_normal.setOnClickListener {
            pageModeHolder = Configuration.PageMode.Normal
            changePageMode(Configuration.PageMode.Normal)
        }
    }

    private fun changePageMode(mode: Int) {
        when (mode) {
            Configuration.PageMode.Page -> {
                ll_page_mode_page.setBackgroundResource(R.drawable.mode_page_shape_corner)
                ll_page_mode_normal.setBackgroundResource(0)
            }
            Configuration.PageMode.Normal -> {
                ll_page_mode_page.setBackgroundResource(0)
                ll_page_mode_normal.setBackgroundResource(R.drawable.mode_page_shape_corner)
            }
        }
    }

    private fun setToolBar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "设置"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    override fun onDestroy() {
        super.onDestroy()
        Configuration.pageMode = pageModeHolder
    }
    companion object {
        fun start(context: Context) = context.startActivity(Intent(context, SettingActivity::class.java))
    }
}
