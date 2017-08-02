package me.jbusdriver.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import com.cfzx.utils.CacheLoader
import jbusdriver.me.jbusdriver.BuildConfig
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.nav_header_main.view.*
import me.jbusdriver.common.*
import me.jbusdriver.mvp.MainContract
import me.jbusdriver.mvp.bean.UpdateBean
import me.jbusdriver.mvp.presenter.MainPresenterImpl
import me.jbusdriver.ui.data.DataSourceType
import me.jbusdriver.ui.fragment.ActressListFragment
import me.jbusdriver.ui.fragment.GenrePagesFragment
import me.jbusdriver.ui.fragment.HomeMovieListFragment
import me.jbusdriver.ui.fragment.MineCollectFragment

class MainActivity : AppBaseActivity<MainContract.MainPresenter, MainContract.MainView>(), NavigationView.OnNavigationItemSelectedListener, MainContract.MainView {

    private val navigationView by lazy { findViewById(R.id.nav_view) as NavigationView }
    private lateinit var selectMenu: MenuItem
    private val fragments: Map<Int, BaseFragment> by lazy {
        mapOf(
                R.id.mine_collect to MineCollectFragment.newInstance() as BaseFragment,
                R.id.movie_ma to HomeMovieListFragment.newInstance(DataSourceType.CENSORED),
                R.id.movie_uncensored to HomeMovieListFragment.newInstance(DataSourceType.UNCENSORED),
                R.id.movie_xyz to HomeMovieListFragment.newInstance(DataSourceType.XYZ),
                R.id.movie_hd to HomeMovieListFragment.newInstance(DataSourceType.GENRE_HD),
                R.id.movie_sub to HomeMovieListFragment.newInstance(DataSourceType.Sub),
                R.id.movie_ma_actress to ActressListFragment.newInstance(DataSourceType.ACTRESSES),
                R.id.movie_uncensored_actress to ActressListFragment.newInstance(DataSourceType.UNCENSORED_ACTRESSES),
                R.id.movie_xyz_actress to ActressListFragment.newInstance(DataSourceType.XYZ_ACTRESSES),
                R.id.movie_ma_genre to GenrePagesFragment.newInstance(DataSourceType.GENRE),
                R.id.movie_uncensored_genre to GenrePagesFragment.newInstance(DataSourceType.UNCENSORED_GENRE),
                R.id.movie_xyz_genre to GenrePagesFragment.newInstance(DataSourceType.XYZ_GENRE)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        initFragments()
        val menuId = savedInstanceState?.getInt("MenuSelectedItemId", R.id.movie_ma) ?: R.id.movie_ma
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
        }

        navigationView.setNavigationItemSelectedListener(this)
        selectMenu = navigationView.menu.findItem(menuId)
        navigationView.setCheckedItem(selectMenu.itemId)
        onNavigationItemSelected(selectMenu)
    }


    private fun initFragments() {
        val ft = supportFragmentManager.beginTransaction()
        fragments.forEach {
            (k, v) ->
            ft.add(R.id.content_main, v, k.toString()).hide(v)
        }
        ft.commit()
    }


    override fun onPostResume() {
        super.onPostResume()

    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
            if (!BuildConfig.DEBUG) {
                moveTaskToBack(false)
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
        if (data is UpdateBean) {
            MaterialDialog.Builder(this).title("更新(${data.versionName})")
                    .content(data.desc)
                    .neutralText("下次更新")
                    .neutralColor(R.color.secondText)
                    .positiveText("更新")
                    .onPositive { _, _ ->
                        browse(data.url)
                    }
                    .show()
        }
    }

    companion object {
        fun start(current: Activity) {
            current.startActivity(Intent(current, MainActivity::class.java))
        }
    }
}
