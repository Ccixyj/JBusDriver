package me.jbusdriver.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import jbusdriver.me.jbusdriver.BuildConfig
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.nav_header_main.view.*
import me.jbusdriver.common.*
import me.jbusdriver.mvp.MainContract
import me.jbusdriver.mvp.bean.*
import me.jbusdriver.mvp.presenter.MainPresenterImpl
import me.jbusdriver.ui.data.AppConfiguration
import java.util.concurrent.TimeUnit

class MainActivity : AppBaseActivity<MainContract.MainPresenter, MainContract.MainView>(), NavigationView.OnNavigationItemSelectedListener, MainContract.MainView {

    private val navigationView by lazy { findViewById<NavigationView>(R.id.nav_view) }
    private var selectMenu: MenuItem? = null
    private val fragments by lazy { hashMapOf<Int, BaseFragment>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) intent.putExtras(savedInstanceState)
        initNavigationView()
        initFragments()
        bindRx()
    }


    private fun initNavigationView() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()


        navigationView.getHeaderView(0).apply {
            tv_app_version.text = packageInfo?.versionName ?: "未知版本"
            ll_git_url.setOnClickListener {
                browse("https://github.com/Ccixyj/JBusDriver")
            }
            ll_telegram.setOnClickListener {
                browse("https://t.me/joinchat/HBJbEA-ka9TcWzaxjmD4hw")
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


            fun tintTextLeftDrawable(parent: ViewGroup) {
                KLog.d("ViewGroup : ${parent.id} =$parent")

                (0..parent.childCount).forEachIndexed { i, _ ->
                    //如果是容器,直接查子view
                    (parent.getChildAt(i) as? ViewGroup)?.let {
                        Schedulers.trampoline().scheduleDirect {
                            tintTextLeftDrawable(it)
                        }

                    } ?: (parent.getChildAt(i) as? TextView)?.compoundDrawables?.forEach {
                        if (it != null)
                            DrawableCompat.setTint(it, R.color.colorAccent.toColorInt())
                    }
                }
            }
            if (Build.VERSION.SDK_INT < 23 && this as? ViewGroup != null) {
                Schedulers.single().scheduleDirect {
                    Schedulers.trampoline().scheduleDirect {
                        tintTextLeftDrawable(this)
                    }.addTo(rxManager)
                }
            }


        }
        navigationView.setNavigationItemSelectedListener(this)

    }


    private fun initFragments() {
        KLog.d("init menuConfig : ${AppConfiguration.menuConfig.filter { it.value }}")
        if (fragments.isNotEmpty()) {
            val ft = supportFragmentManager.beginTransaction()
            fragments.forEach {
                supportFragmentManager.findFragmentByTag(it.key.toString())?.let {
                    ft.remove(it)
                }
                ft.remove(it.value)
            }
            fragments.clear()
            ft.commitAllowingStateLoss()
        }
        MenuOp.Ops.forEach {
            navigationView.menu.findItem(it.id).isVisible = it.isHow
        }
        setNavSelected()
    }

    private fun setNavSelected() {
        val id = (MenuOp.Ops - MenuOp.mine).find { it.isHow }?.id
                ?: MenuOp.Ops.find { it.isHow }?.id ?: let {
                    toast("至少配置一项菜单!!!!")
                    return
                }
        val menuId = intent.getIntExtra("MenuSelectedItemId", id)
        selectMenu = navigationView.menu.findItem(menuId)
        KLog.d("selectMenu $selectMenu")
        selectMenu?.let {
            navigationView.setCheckedItem(it.itemId)
            onNavigationItemSelected(it)
        }

    }

    private fun bindRx() {
        RxBus.toFlowable(MenuChangeEvent::class.java)
                .delay(100, TimeUnit.MILLISECONDS) //稍微延迟,否则设置可能没有完成
                .compose(SchedulersCompat.computation())
                .subscribeBy {
                    initFragments()
                }
                .addTo(rxManager)

        RxBus.toFlowable(CategoryChangeEvent::class.java)
                .debounce(500, TimeUnit.MILLISECONDS) //稍微延迟,否则设置可能没有完成
                .compose(SchedulersCompat.computation())
                .subscribeBy {
                    fragments.remove(R.id.mine_collect)?.let {
                        KLog.d("remove :$it $selectMenu")
                        supportFragmentManager.beginTransaction().remove(it).commitNowAllowingStateLoss()
                    }
                    if (selectMenu?.itemId == R.id.mine_collect) setNavSelected()

                }.addTo(rxManager)

    }


    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
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
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        supportActionBar?.title = selectMenu?.title
        return true
    }

    private fun switchFragment(itemId: Int) {
        val ft = supportFragmentManager.beginTransaction()
        val replace = fragments.getOrPut(itemId) {
            MenuOp.Ops.find { it.id == itemId }?.initializer?.invoke()?.apply {
                ft.add(R.id.content_main, this, itemId.toString())
            } ?: error("no matched fragment")
        }
        supportFragmentManager.findFragmentByTag(selectMenu?.itemId.toString())?.let {
            ft.hide(it)
        }
        ft.show(replace)
        ft.commitAllowingStateLoss()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        selectMenu?.let {
            outState?.putInt("MenuSelectedItemId", it.itemId)
        }
        super.onSaveInstanceState(outState)
    }

    override fun createPresenter() = MainPresenterImpl()

    override val layoutId = R.layout.activity_main


    @SuppressLint("ResourceAsColor")
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

    @SuppressLint("ResourceAsColor")
    private fun showNotice(notice: Any?) {
        val sharfp by lazy { getSharedPreferences("config", Context.MODE_PRIVATE) }
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
