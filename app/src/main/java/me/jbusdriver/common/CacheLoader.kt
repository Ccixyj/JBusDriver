package com.cfzx.utils

import android.support.v4.util.LruCache
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import me.jbusdriver.common.ACache
import me.jbusdriver.common.AppContext
import me.jbusdriver.common.KLog
import me.jbusdriver.common.formatFileSize
import java.util.concurrent.TimeUnit

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

    /*============================cache====================================*/
    fun cacheLruAndDisk(pair: Pair<String, Any>, seconds: Int? = null) = with(AppContext.gson.toJson(pair.second)) {
        lru.put(pair.first, this)
        seconds?.let { acache.put(pair.first, AppContext.gson.toJson(pair.second), seconds) } ?: acache.put(pair.first, AppContext.gson.toJson(pair.second))
    }

    fun cacheLru(pair: Pair<String, Any>) = lru.put(pair.first, AppContext.gson.toJson(pair.second))
    fun cacheDisk(pair: Pair<String, Any>, seconds: Int? = null) = seconds?.let { acache.put(pair.first, AppContext.gson.toJson(pair.second), seconds) } ?: acache.put(pair.first, AppContext.gson.toJson(pair.second))


    /*============================cache to flowable====================================*/
    fun fromLruAsync(key: String): Flowable<String> = Flowable.interval(0, 300, TimeUnit.MILLISECONDS, Schedulers.io()).flatMap {
        val v = lru[key]
        KLog.d("fromLruAsync : $key ,$v")
        v?.let { Flowable.just(it) } ?: Flowable.empty()
    }.timeout(35, TimeUnit.SECONDS, Flowable.empty()).take(1).subscribeOn(Schedulers.io())

    fun fromDiskAsync(key: String, add2Lru: Boolean = true): Flowable<String> = Flowable.interval(0, 300, TimeUnit.MILLISECONDS, Schedulers.io()).flatMap {
        val v = acache.getAsString(key)
        KLog.d("fromDiskAsync : $key ,$v")
        v?.let { Flowable.just(it) } ?: Flowable.empty()
    }.timeout(35, TimeUnit.SECONDS, Flowable.empty()).take(1).doOnNext { if (add2Lru) lru.put(key, it) }.subscribeOn(Schedulers.io())


    fun justLru(key: String) = Flowable.just(lru[key].apply { KLog.d("justLru : $key ,$this") }).filter { it != null }
    fun justDisk(key: String, add2Lru: Boolean = true) = Flowable.just(acache.getAsString(key).apply { KLog.d("justDisk : $key add lru $add2Lru,$this") }).filter { it != null }

    /*===============================remove cache=====================================*/
    fun removeCacheLike(vararg keys: String, isRegex: Boolean = false) {
        Schedulers.computation().createWorker().schedule {
            lru.snapshot().keys.let {
                cacheCopyKeys ->
                keys.forEach { removeKey ->
                    val filterAction: (String) -> Boolean = { s -> if (isRegex) s.contains(removeKey.toRegex()) else s.contains(removeKey) }
                    cacheCopyKeys.filter(filterAction).forEach {
                        KLog.d("removeCacheLike : $it")
                        cacheCopyKeys.remove(it); lru.remove(it);acache.remove(it)
                    }
                }
            }

        }
    }

}
