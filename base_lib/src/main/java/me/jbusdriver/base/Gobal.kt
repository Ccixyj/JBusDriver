package me.jbusdriver.base

import android.content.Context
import android.net.Uri
import android.support.v4.util.LruCache
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.orhanobut.logger.Logger
import java.io.File
import java.lang.reflect.Modifier.TRANSIENT

typealias  KLog = Logger

val GSON by lazy {
    GsonBuilder().excludeFieldsWithModifiers(TRANSIENT).registerTypeAdapter(Int::class.java, JsonDeserializer<Int> { json, _, _ ->
        if (json.isJsonNull || json.asString.isEmpty()) {
            return@JsonDeserializer null
        }
        try {
            return@JsonDeserializer json.asInt
        } catch (e: NumberFormatException) {
            return@JsonDeserializer null
        }
    }).serializeNulls().create()
}

private val TOAST: Toast by lazy { Toast.makeText(JBusManager.context, "", Toast.LENGTH_LONG) }

fun Context.toast(str: String, duration: Int = Toast.LENGTH_LONG) {
    postMain {
        TOAST.setText(str)
        TOAST.duration = duration
        TOAST.show()
    }

}




fun createDir(collectDir: String): String? {
    File(collectDir.trim()).let {
        try {
            if (!it.exists() && it.mkdirs()) return collectDir
            if (it.exists()) {
                if (it.isDirectory) {
                    return collectDir
                } else {
                    it.delete()
                    createDir(collectDir) //recreate
                }
            }
        } catch (e: Exception) {
//            MobclickAgent.reportError(JBus, e)
        }
    }
    return null
}

private val urlCache by lazy { LruCache<String, Uri>(512) }

//string url -> get url host
val String.urlHost: String
    get() = (urlCache.get(this) ?: let {
        val uri = Uri.parse(this)
        urlCache.put(this, uri)
        uri
    }).let {
        checkNotNull(it)
        "${it.scheme}://${it.host}"
    }


val String.urlPath: String
    get() = (urlCache.get(this) ?: let {
        val uri = Uri.parse(this)
        urlCache.put(this, uri)
        uri
    }).let {
        checkNotNull(it)
        it.path
    }