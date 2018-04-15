package me.jbusdriver.ui.task

import android.app.IntentService
import android.content.Context
import android.content.Intent
import me.jbusdriver.mvp.model.RecommendModel

private const val ACTION_TRIM_LIKE = "me.jbusdriver.ui.task.action.trim_like"

class TrimLikeService : IntentService("TrimLikeService") {

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_TRIM_LIKE -> {
                handleTrim()
            }
        }
    }

    private fun handleTrim() {
        RecommendModel.trimCache()
    }

    companion object {

        fun startTrimSize(context: Context) {
            val intent = Intent(context, TrimLikeService::class.java)
            intent.action = ACTION_TRIM_LIKE
            context.startService(intent)
        }
    }

}
