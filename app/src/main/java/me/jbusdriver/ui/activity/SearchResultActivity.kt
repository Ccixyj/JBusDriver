package me.jbusdriver.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.activity_search_result.*
import me.jbusdriver.base.RxBus
import me.jbusdriver.base.common.BaseActivity
import me.jbusdriver.base.common.C
import me.jbusdriver.mvp.bean.SearchWord
import me.jbusdriver.ui.fragment.SearchResultPagesFragment

class SearchResultActivity : BaseActivity() {

    private val searchWord by lazy { intent.getStringExtra(C.BundleKey.Key_1) ?: error("must set search word") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(searchWord)
        //go to SearchResultPagesFragment
        supportFragmentManager.beginTransaction().replace(R.id.fl_search_pages, SearchResultPagesFragment().apply {
            arguments = Bundle().apply { putString(C.BundleKey.Key_1, searchWord) }
        }).commit()

        RxBus.toFlowable(SearchWord::class.java).subscribeBy{
           setTitle(it.query)
        } .addTo(rxManager)
    }

    private fun setTitle(title: String) {
        supportActionBar?.title = "$title 的搜索结果"
    }

    companion object {
        fun start(context: Context, searchWord: String) {
            context.startActivity(Intent(context, SearchResultActivity::class.java).apply {
                putExtra(C.BundleKey.Key_1, searchWord)
            })
        }
    }
}
