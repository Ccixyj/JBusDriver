package me.jbusdriver.plugin.magnet.loaders

import android.util.Base64
import java.util.zip.Deflater
import java.util.zip.DeflaterInputStream

object Helper {

    fun encodeBase64(str: String) =
        Base64.encodeToString(str.toByteArray(), Base64.NO_PADDING or Base64.URL_SAFE).trim()


    fun gzDeflateBase64(str: String):String {
        return  DeflaterInputStream(str.toByteArray().inputStream(), Deflater(Deflater.DEFLATED,true)).use {
            Base64.encodeToString(it.readBytes(), Base64.NO_PADDING or Base64.URL_SAFE).trim()
        }
    }
}