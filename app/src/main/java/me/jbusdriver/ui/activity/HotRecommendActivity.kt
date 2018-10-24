package me.jbusdriver.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.activity_hot_recommend.*
import me.jbusdriver.base.common.BaseActivity
import me.jbusdriver.ui.fragment.RecommendListFragment

@Deprecated("not user any more")
class HotRecommendActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hot_recommend)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "热门推荐"

        supportFragmentManager.beginTransaction().replace(R.id.fl_hot_recommend, RecommendListFragment.newInstance()).commit()
    }


    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, HotRecommendActivity::class.java))
        }
    }
}
