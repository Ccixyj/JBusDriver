package me.jbusdriver.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.activity_search_result.*
import me.jbusdriver.common.BaseActivity
import me.jbusdriver.common.C

class SearchResultActivity : BaseActivity() {

    private val searchWord by lazy { intent.getStringExtra(C.BundleKey.Key_1)  ?: error("must set search word")}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "$searchWord 的搜索结果"
        //go to SearchResultPagesFragment
    }

    companion object {
        fun start(context: Context, searchWord: String) {
            context.startActivity(Intent(context, SearchResultActivity::class.java).apply {
                putExtra(C.BundleKey.Key_1,searchWord)
            })
        }
    }
}
