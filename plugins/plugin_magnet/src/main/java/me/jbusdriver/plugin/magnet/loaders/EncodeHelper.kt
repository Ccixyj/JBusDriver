package me.jbusdriver.plugin.magnet.loaders

import android.util.Base64
import java.net.URLEncoder
import java.util.zip.Deflater
import java.util.zip.DeflaterInputStream

object EncodeHelper {

    fun encodeBase64(str: String) =
        Base64.encodeToString(str.toByteArray(), Base64.NO_PADDING or Base64.URL_SAFE).trim()


    fun gzDeflateBase64(str: String): String {
        return DeflaterInputStream(str.toByteArray().inputStream(), Deflater(Deflater.DEFLATED, true)).use {
            Base64.encodeToString(it.readBytes(), Base64.NO_PADDING or Base64.URL_SAFE).trim()
        }
    }

    fun utf8Encode(str: String): String = URLEncoder.encode(str, Charsets.UTF_8.name())

    private val CHARS by lazy { "0123456789abcdef".toCharArray() }
    fun str2HexStr(str: String): String {
        val sb = StringBuilder("")
        val bs = str.toByteArray()
        var bit: Int
        for (i in bs.indices) {
            bit = bs[i].toInt() and 0x0f0 shr 4
            sb.append(CHARS[bit])
            bit = bs[i].toInt() and 0x0f
            sb.append(CHARS[bit])
        }
        return sb.toString().trim()
    }

}