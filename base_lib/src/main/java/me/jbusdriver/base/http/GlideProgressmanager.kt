package me.jbusdriver.base.http

import com.bumptech.glide.load.engine.GlideException
import me.jbusdriver.base.KLog
import okhttp3.ResponseBody
import okio.*
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList


interface OnProgressListener {

    fun onProgress(imageUrl: String, bytesRead: Long, totalBytes: Long, isDone: Boolean, exception: GlideException?)
}

private val listeners by lazy { CopyOnWriteArrayList<WeakReference<OnProgressListener>>() }

val GlideProgressListener
    get() = object : OnProgressListener {
        override fun onProgress(imageUrl: String, bytesRead: Long, totalBytes: Long, isDone: Boolean, exception: GlideException?) {
            if (listeners.isEmpty()) return
            listeners.forEach {
                it.get()?.onProgress(imageUrl, bytesRead, totalBytes, isDone, exception)
                        ?: listeners.remove(it)
            }
        }
    }


fun addProgressListener(progressListener: OnProgressListener) {
    if (findProgressListener(progressListener) == null) {
        listeners.add(WeakReference(progressListener))
    }
    KLog.d("add progress $progressListener success :$listeners")
}

fun removeProgressListener(progressListener: OnProgressListener) {
    val listener = findProgressListener(progressListener)
    if (listener != null) {
        listeners.remove(listener)
    }
    KLog.d("remove progress $listener success :$listeners")
}

private fun findProgressListener(listener: OnProgressListener): WeakReference<OnProgressListener>? = listeners.find { it.get() == listener }

class ProgressResponseBody(private val imageUrl: String, private val responseBody: ResponseBody?, private val progressListener: OnProgressListener?) : ResponseBody() {
    private var bufferedSource: BufferedSource? = null

    override fun contentType() = responseBody?.contentType()

    override fun contentLength() = responseBody?.contentLength() ?: 0L

    override fun source(): BufferedSource? {
        if (bufferedSource == null && responseBody != null) {
            bufferedSource = Okio.buffer(source(responseBody.source()))
        }
        return bufferedSource
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            private var totalBytesRead: Long = 0

            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                totalBytesRead += if (bytesRead == -1L) 0 else bytesRead

                progressListener?.onProgress(imageUrl, totalBytesRead, contentLength(), bytesRead == -1L, null)
                return bytesRead
            }
        }
    }
}