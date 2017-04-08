package com.cfzx.utils

import android.support.v4.util.LruCache
import me.jbusdriver.common.ACache
import me.jbusdriver.common.AppContext
import me.jbusdriver.common.KLog
import me.jbusdriver.common.formatFileSize

object CacheLoader {
    private val TAG = "CacheLoader"

    private fun initMemCache(): LruCache<String, String> {
        val maxMemory = Runtime.getRuntime().maxMemory().toInt()
        val cacheSize = if (maxMemory / 8 > 4 * 1024 * 1024) 4 * 1024 * 1024 else maxMemory
        KLog.t(TAG).d("max cacheSize = ${cacheSize.toLong().formatFileSize()}")
        return object : LruCache<String, String>(cacheSize) { //4m
            override fun entryRemoved(evicted: Boolean, key: String?, oldValue: String?, newValue: String?) {
                KLog.d(String.format("entryRemoved : evicted = %s , key = %20s , oldValue = %30s , newValue = %30s", evicted.toString(), key, oldValue, newValue))
                if (evicted) oldValue.let { null } ?: oldValue.let { newValue }
            }

            override fun sizeOf(key: String, value: String): Int {
                val length = value.toByteArray().size
                KLog.d("key = $key  sizeOf = [$length]bytes format:${(this.size() + length).toLong().formatFileSize()}")
                return length
            }

        }
    }

    @JvmStatic val lru: LruCache<String, String> by lazy {
        initMemCache()
    }


    @JvmStatic val acache: ACache  by lazy {
        ACache.get(AppContext.instace)
    }

}
