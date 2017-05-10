package me.jbusdriver.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle

import jbusdriver.me.jbusdriver.R
import me.jbusdriver.common.BaseActivity

class MovieListActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moive_list)
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, MovieListActivity::class.java))
        }
    }

}
