package me.jbusdriver.base

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.support.annotation.Nullable
import android.support.v4.util.ArrayMap
import android.text.format.Formatter
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

//region Size

const val KB = 1024.0
const val MB = KB * 1024
const val GB = MB * 1024
const val TB = GB * 1024

fun Long.formatFileSize(): String = Formatter.formatFileSize(JBusManager.manager.first().get(), this)
//endregion

//region array map
fun <K, V> arrayMapof(vararg pairs: Pair<K, V>): ArrayMap<K, V> = ArrayMap<K, V>(pairs.size).apply { putAll(pairs) }

fun <K, V> arrayMapof(): ArrayMap<K, V> = ArrayMap()
//endregion

//region convert
fun Int.toColorInt() = getColor(this)

private fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        JBusManager.context.resources.getColor(id, null)
    } else JBusManager.context.resources.getColor(id)
}
//endregion


//region Context
val Main_Worker by lazy { AndroidSchedulers.mainThread().createWorker() }

fun postMain(block: () -> Unit) = Main_Worker.schedule(block)


val Context.inflater: LayoutInflater
    get() = LayoutInflater.from(this)

val Context.displayMetrics: DisplayMetrics
    get() = resources.displayMetrics


fun Context.dpToPx(dp: Float) = (dp * this.displayMetrics.density + 0.5).toInt()

fun Context.pxToDp(px: Float) = (px / this.displayMetrics.density + 0.5).toInt()


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
        this.packageManager.getPackageInfo(
                this.packageName, 0)
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
            flags = Intent.FLAG_ACTIVITY_NEW_TASK

        })
    } catch (e: Exception) {
        errorHandler(e)
    }
}


