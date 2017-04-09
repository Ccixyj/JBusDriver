package me.jbusdriver.ui.activity

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.util.SparseArray
import android.view.Menu
import android.view.MenuItem
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.common.AppBaseActivity
import me.jbusdriver.common.KLog
import me.jbusdriver.mvp.MainContract
import me.jbusdriver.mvp.presenter.MainPresenterImpl
import me.jbusdriver.ui.fragment.JapanMovieFragment

class MainActivity : AppBaseActivity<MainContract.MainPresenter, MainContract.MainView>(), NavigationView.OnNavigationItemSelectedListener, MainContract.MainView {

    val fragments = SparseArray<android.support.v4.app.Fragment>(6)

    lateinit var selectMenu: MenuItem
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        val menuId = savedInstanceState?.getInt("MenuSelectedItemId", R.id.movie_ma) ?: R.id.movie_ma
        val navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
        selectMenu = navigationView.menu.findItem(menuId)
        navigationView.setCheckedItem(selectMenu.itemId)
        onNavigationItemSelected(selectMenu)
    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        selectMenu = item
        val id = item.itemId
        KLog.d("onNavigationItemSelected $item ")
        val fragment = fragments.get(id) ?: (when (id) {
            R.id.movie_ma -> JapanMovieFragment.newInstance()
            R.id.movie_no_ma -> JapanMovieFragment.newInstance()
            R.id.nav_slideshow -> JapanMovieFragment.newInstance()
            R.id.nav_manage -> JapanMovieFragment.newInstance()
            R.id.nav_share -> JapanMovieFragment.newInstance()
            R.id.nav_send -> JapanMovieFragment.newInstance()
            else -> error("no matched fragment")
        }.apply { fragments.put(id, this) })
        //
        supportFragmentManager.beginTransaction().replace(R.id.content_main,fragment,fragment::class.java.simpleName).commit()
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("MenuSelectedItemId", selectMenu.itemId)
        super.onSaveInstanceState(outState)
    }

    override fun createPresenter() = MainPresenterImpl()

    override val layoutId = R.layout.activity_main


}
