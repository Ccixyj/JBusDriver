package me.jbusdriver.mvp.model

import com.google.gson.JsonObject
import me.jbusdriver.base.ACache
import me.jbusdriver.base.GSON
import me.jbusdriver.base.KLog
import me.jbusdriver.base.fromJson
import me.jbusdriver.common.JBus
import java.util.*

@Deprecated("not user any more")
object RecommendModel {
    private const val COUNT = "count"
    private const val UID = "uid"

    private val likeCache by lazy { ACache.get(JBus, "like") }

    fun trimCache() {
        val dir = likeCache.dir()
        if (dir.isDirectory) {
            dir.walk().maxDepth(1).forEach {
                KLog.d("on onLeave ${it.absolutePath}")
                try {
                    if (it.isFile && ACache.Utils.isDue(it.readText())) {
                        it.delete()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    KLog.w("scan file error $e")
                }
            }
        }
    }


    private fun calMillSeconds(): Long {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        cal.time = Date(now)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.add(Calendar.DATE, 1)
        return cal.timeInMillis - now   // 1s 误差
    }


    private fun generateUid(likeKey: String) = likeKey + UUID.randomUUID().toString()
    private fun getLikeJson(likeKey: String) = likeCache.getAsString(likeKey)?.let {
        GSON.fromJson<JsonObject>(it)
    } ?: JsonObject()

    /**
     *当天点赞次数
     */
    fun getLikeCount(likeKey: String) = getLikeJson(likeKey).get(COUNT)?.asInt ?: 0

    fun getLikeUID(likeKey: String) = getLikeJson(likeKey).get(UID)?.asString
            ?: generateUid(likeKey)


    /**
     * 允许误差1s
     */
    fun save(likeKey: String, uid: String) {
        try {
            val json = getLikeJson(likeKey)
            val new = calMillSeconds() <= 1000 || (json.get(COUNT)?.asInt ?: 0) <= 0
            if (new) {
                val newJson = JsonObject().apply {
                    addProperty(COUNT, 1)
                    addProperty(UID, uid)
                }
                likeCache.put(likeKey, newJson.toString(), (calMillSeconds() / 1000).toInt() - 1)
            } else {
                likeCache.put(likeKey, json.apply {
                    addProperty(COUNT, json.get(COUNT)?.asInt?.plus(1) ?: 1)
                    if (json.get(UID)?.asString != uid) {
                        addProperty(UID, uid)
                    }
                }.toString(), (calMillSeconds() / 1000).toInt() - 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            KLog.e("error: $e")
        }
    }

}