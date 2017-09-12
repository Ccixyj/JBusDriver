package me.jbusdriver.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import com.cfzx.utils.CacheLoader
import jbusdriver.me.jbusdriver.BuildConfig
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.nav_header_main.view.*
import me.jbusdriver.common.*
import me.jbusdriver.mvp.MainContract
import me.jbusdriver.mvp.bean.MenuOp
import me.jbusdriver.mvp.bean.NoticeBean
import me.jbusdriver.mvp.bean.UpdateBean
import me.jbusdriver.mvp.presenter.MainPresenterImpl

class MainActivity : AppBaseActivity<MainContract.MainPresenter, MainContract.MainView>(), NavigationView.OnNavigationItemSelectedListener, MainContract.MainView {

    private val navigationView by lazy { findViewById(R.id.nav_view) as NavigationView }
    private lateinit var selectMenu: MenuItem
    private val sharfp by lazy { getSharedPreferences("config", Context.MODE_PRIVATE) }
    private val fragments by lazy { hashMapOf<Int, BaseFragment>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigationView()
        initFragments(savedInstanceState)

    }


    private fun initNavigationView() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()


        navigationView.getHeaderView(0).apply {
            tv_app_version.text = packageInfo?.versionName ?: "未知版本"
            ll_git_url.setOnClickListener {
                browse("https://github.com/Ccixyj/JBusDriver")
            }
            ll_click_reload.setOnClickListener {
                CacheLoader.lru.evictAll()
                CacheLoader.acache.remove(C.Cache.BUS_URLS)
                SplashActivity.start(this@MainActivity)
                finish()
            }

            tv_app_setting.setOnClickListener {
                KLog.d("tv_app_setting")
                SettingActivity.start(this@MainActivity)
                drawer.closeDrawer(GravityCompat.START)
            }
        }
        navigationView.setNavigationItemSelectedListener(this)

    }


    private fun initFragments(savedInstanceState: Bundle?) {
        fragments.clear()
        MenuOp.Ops.forEach {
            if (it.isHow) {
                fragments.put(it.id, it.initializer.invoke())
            }
            navigationView.menu.findItem(it.id).isVisible = it.isHow
        }
        val ft = supportFragmentManager.beginTransaction()
        supportFragmentManager.fragments?.forEach {
            ft.remove(it)
        }
        fragments.onEach { (k, v) ->
            ft.add(R.id.content_main, v, k.toString()).hide(v)
        }
        ft.commit()
        setNavSelected(savedInstanceState)
    }

    private fun setNavSelected(savedInstanceState: Bundle?) {
        val id = (MenuOp.Ops - MenuOp.mine).firstOrNull()?.id ?: MenuOp.Ops.firstOrNull()?.id ?: error("至少配置一项菜单!!!!")
        val menuId = savedInstanceState?.getInt("MenuSelectedItemId", id) ?: id
        selectMenu = navigationView.menu.findItem(menuId)
        navigationView.setCheckedItem(selectMenu.itemId)
        onNavigationItemSelected(selectMenu)
    }

    override fun onPostResume() {
        super.onPostResume()

    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            if (!BuildConfig.DEBUG) {
                moveTaskToBack(false)
            } else {
                super.onBackPressed()
            }
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        switchFragment(item.itemId)
        selectMenu = item
        KLog.d("onNavigationItemSelected $item ")
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        supportActionBar?.title = selectMenu.title
        return true
    }

    private fun switchFragment(itemId: Int) {
        val replace = fragments[itemId] ?: error("no match fragment for menu $itemId")

        val ft = supportFragmentManager.beginTransaction()
        supportFragmentManager.findFragmentByTag(selectMenu.itemId.toString())?.let {
            ft.hide(it)
        }
        ft.show(replace)
        ft.commitAllowingStateLoss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("MenuSelectedItemId", selectMenu.itemId)
        super.onSaveInstanceState(outState)
    }

    override fun createPresenter() = MainPresenterImpl()

    override val layoutId = R.layout.activity_main


    override fun <T> showContent(data: T?) {
        if (data is Pair<*, *> && data.first is UpdateBean) {
            val bean = data.first as UpdateBean
            if (viewContext.packageInfo?.versionCode ?: -1 < bean.versionCode) {
                MaterialDialog.Builder(this).title("更新(${bean.versionName})")
                        .content(bean.desc)
                        .neutralText("下次更新")
                        .neutralColor(R.color.secondText)
                        .positiveText("更新")
                        .onPositive { _, _ ->
                            browse(bean.url)
                        }
                        .dismissListener {
                            showNotice(data.second)
                        }
                        .show()
            } else {
                showNotice(data.second)
            }
        }

    }

    private fun showNotice(notice: Any?) {
        if (notice != null && notice is NoticeBean && !TextUtils.isEmpty(notice.content) && notice.id > 0 && sharfp.getInt(NoticeIgnoreID, -1) < notice.id) {
            MaterialDialog.Builder(this).title("公告")
                    .content(notice.content!!)
                    .neutralText("忽略该提示")
                    .neutralColor(R.color.secondText)
                    .onNeutral { _, _ ->
                        sharfp.edit().putInt(NoticeIgnoreID, notice.id).apply()
                    }
                    .positiveText("知道了")
                    .show()
        }
    }

    companion object {
        const val NoticeIgnoreID = "notice_ignore_id"
        fun start(current: Activity) {
            current.startActivity(Intent(current, MainActivity::class.java))
        }
    }
}
