package me.jbusdriver.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import com.chad.library.adapter.base.entity.MultiItemEntity
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.activity_setting.*
import me.jbusdriver.common.BaseActivity
import me.jbusdriver.common.spanCount
import me.jbusdriver.mvp.bean.MenuOp
import me.jbusdriver.mvp.bean.MenuOpHead
import me.jbusdriver.ui.adapter.Expand_Type_Head
import me.jbusdriver.ui.adapter.MenuOpAdapter
import me.jbusdriver.ui.data.AppConfiguration

class SettingActivity : BaseActivity() {

    private var pageModeHolder = AppConfiguration.pageMode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setToolBar()
        initSettingView()
    }

    private fun initSettingView() {
        //page mode
        changePageMode(AppConfiguration.pageMode)
        ll_page_mode_page.setOnClickListener {
            pageModeHolder = AppConfiguration.PageMode.Page
            changePageMode(AppConfiguration.PageMode.Page)
        }
        ll_page_mode_normal.setOnClickListener {
            pageModeHolder = AppConfiguration.PageMode.Normal
            changePageMode(AppConfiguration.PageMode.Normal)
        }

        //menu op
        val data: List<MultiItemEntity> = arrayListOf(
                MenuOpHead("个人").apply { MenuOp.mine.forEach { addSubItem(it) };isExpanded = true },
                MenuOpHead("有碼").apply { MenuOp.nav_ma.forEach { addSubItem(it) } },
                MenuOpHead("無碼").apply { MenuOp.nav_uncensore.forEach { addSubItem(it) } },
                MenuOpHead("欧美").apply { MenuOp.nav_xyz.forEach { addSubItem(it) } },
                MenuOpHead("其他").apply { MenuOp.nav_other.forEach { addSubItem(it) } }
        )
        val adapter = MenuOpAdapter(data)
        rv_menu_op.adapter = adapter
        rv_menu_op.layoutManager = GridLayoutManager(viewContext, viewContext.spanCount).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int) =
                        if (adapter.getItemViewType(position) == Expand_Type_Head) spanCount else 1
            }
        }
    }

    private fun changePageMode(mode: Int) {
        when (mode) {
            AppConfiguration.PageMode.Page -> {
                ll_page_mode_page.setBackgroundResource(R.drawable.mode_page_shape_corner)
                ll_page_mode_normal.setBackgroundResource(0)
            }
            AppConfiguration.PageMode.Normal -> {
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
        AppConfiguration.pageMode = pageModeHolder
    }

    companion object {
        fun start(context: Context) = context.startActivity(Intent(context, SettingActivity::class.java))
    }
}
