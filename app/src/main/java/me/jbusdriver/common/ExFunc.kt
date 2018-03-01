package me.jbusdriver.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.support.v4.util.ArrayMap
import android.text.format.Formatter
import android.util.DisplayMetrics
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.orhanobut.logger.Logger
import com.umeng.analytics.MobclickAgent
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.annotations.Nullable
import java.io.File
import java.util.concurrent.TimeUnit

//region Size
typealias  KLog = Logger

const val KB = 1024.0
const val MB = KB * 1024
const val GB = MB * 1024
const val TB = GB * 1024

fun Long.formatFileSize(): String = Formatter.formatFileSize(JBus, this)
//endregion

//region array map
fun <K, V> arrayMapof(vararg pairs: Pair<K, V>): ArrayMap<K, V> = ArrayMap<K, V>(pairs.size).apply { putAll(pairs) }

fun <K, V> arrayMapof(): ArrayMap<K, V> = ArrayMap()
//endregion

//region convert
fun Int.toColorInt() = getColor(this)

private fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        JBus.resources.getColor(id, null)
    } else JBus.resources.getColor(id)
}
//endregion

//region Context
val Context.inflater: LayoutInflater
    get() = LayoutInflater.from(this)

val Context.displayMetrics: DisplayMetrics
    get() = resources.displayMetrics


fun Context.dpToPx(dp: Float) = (dp * this.displayMetrics.density + 0.5).toInt()

fun Context.pxToDp(px: Float) = (px / this.displayMetrics.density + 0.5).toInt()

private val TOAST: Toast by lazy { Toast.makeText(JBus, "", Toast.LENGTH_LONG) }

fun Context.toast(str: String, duration: Int = Toast.LENGTH_LONG) {
    TOAST.setText(str)
    TOAST.duration = duration
    TOAST.show()
}

private fun inflateView(context: Context, layoutResId: Int, parent: ViewGroup?,
                        attachToRoot: Boolean): View =
        LayoutInflater.from(context).inflate(layoutResId, parent, attachToRoot)

fun Context.inflate(layoutResId: Int, parent: ViewGroup? = null, attachToRoot: Boolean = false): View =
        inflateView(this, layoutResId, parent, attachToRoot)
//endregion

//region gson
@Nullable
inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)
//inline fun <reified T> Gson.fromJson(json: JsonElement) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)
//inline fun <reified T> Gson.fromJson(json: Reader) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)
//inline fun <reified T> Gson.fromJson(json: JsonReader) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)

fun Any?.toJsonString() = GSON.toJson(this)
//endregion

//region http
fun <R> Flowable<R>.addUserCase(sec: Int = 12) =
        this.timeout(sec.toLong(), TimeUnit.SECONDS, Schedulers.io()) //超时
                .subscribeOn(Schedulers.io())
                .take(1)
                .filter { it != null }
//endregion

//region screenWidth
val Context.screenWidth: Int
    inline get() {
        val wm = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(this.displayMetrics)
        return displayMetrics.widthPixels
    }

val Context.spanCount: Int
    inline get() = with(this.screenWidth) {
        when {
            this <= 1080 -> 3
            this <= 1440 -> 4
            else -> 5
        }
    }
//endregion

//region copy paste
/**
 * 实现文本复制功能
 * add by wangqianzhou
 * @param content
 */
fun Context.copy(content: String) {
    KLog.d("copy : $content")
    // 得到剪贴板管理器
    val cmb = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cmb.primaryClip = ClipData.newPlainText(null, content)
}

/**
 * 实现粘贴功能
 * add by wangqianzhou
 * *
 * @return
 */
fun Context.paste(): String? {
    // 得到剪贴板管理器
    val cmb = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return cmb.primaryClip?.let {
        if (it.itemCount > 0) it.getItemAt(0).coerceToText(this)?.toString() else null
    }
}
//endregion

//region package info
val Context.packageInfo: PackageInfo?
    get() = try {
        JBus.packageManager.getPackageInfo(
                JBus.packageName, 0)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
//endregion

//region cursor
fun Cursor.getStringByColumn(colName: String): String? =
        try {
            this.getString(this.getColumnIndexOrThrow(colName))
        } catch (ex: Exception) {
            ""
        }

fun Cursor.getIntByColumn(colName: String): Int = try {
    this.getInt(this.getColumnIndexOrThrow(colName))
} catch (ex: Exception) {
    -1
}

fun Cursor.getLongByColumn(colName: String): Long = try {
    this.getLong(this.getColumnIndexOrThrow(colName))
} catch (ex: Exception) {
    -1
}
//endregion


fun Context.browse(url: String, errorHandler: (Throwable) -> Unit = {}) {
    try {

        startActivity(Intent().apply {
            this.action = "android.intent.action.VIEW"
            this.data = Uri.parse(url)
        })
    } catch (e: Exception) {
        toast("无法处理该类型的链接")
        errorHandler(e)
    }
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


/*glide : url ->? custome glideurl */
val String.toGlideUrl: GlideNoHost
    inline get() = GlideNoHost(this) /**/


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
            MobclickAgent.reportError(JBus, e)
        }
    }
    return null
}
