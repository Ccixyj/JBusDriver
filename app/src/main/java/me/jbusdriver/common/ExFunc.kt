package me.jbusdriver.common

import com.orhanobut.logger.Logger
import java.util.*

/**
 * Created by Administrator on 2017/4/8.
 */
typealias  KLog = Logger
enum class SizeUnit {
    Byte,
    KB,
    MB,
    GB,
    TB,
    Auto
}

fun  Long.formatFileSize(unit: SizeUnit = SizeUnit.Auto): String {
    var unit = unit
    if (this < 0) {
        return "未知大小"
    }

    val KB = 1024.0
    val MB = KB * 1024
    val GB = MB * 1024
    val TB = GB * 1024
    if (unit == SizeUnit.Auto) {
        if (this < KB) {
            unit = SizeUnit.Byte
        } else if (this < MB) {
            unit = SizeUnit.KB
        } else if (this < GB) {
            unit = SizeUnit.MB
        } else if (this < TB) {
            unit = SizeUnit.GB
        } else {
            unit = SizeUnit.TB
        }
    }

    when (unit) {
        SizeUnit.Byte -> return this.toString() + "B"
        SizeUnit.KB -> return String.format(Locale.US, "%.2fKB", this / KB)
        SizeUnit.MB -> return String.format(Locale.US, "%.2fMB", this / MB)
        SizeUnit.GB -> return String.format(Locale.US, "%.2fGB", this / GB)
        SizeUnit.TB -> return String.format(Locale.US, "%.2fPB", this / TB)
        else -> return this.toString() + "B"
    }

}

