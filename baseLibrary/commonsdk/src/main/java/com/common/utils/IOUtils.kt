package com.common.utils

import android.text.TextUtils
import com.common.log.MyLog
import okio.BufferedSink
import okio.BufferedSource
import okio.Okio
import java.io.File
import java.nio.charset.Charset

class IOUtils {
    fun writeFile(content: String, file: File) {
        if (TextUtils.isEmpty(content)) {
            return
        }
        var bufferSink: BufferedSink? = null
        try {
            bufferSink = Okio.buffer(Okio.sink(file))
            bufferSink.writeString(content, Charset.forName("utf-8"))
        } catch (e: Exception) {
            MyLog.e(e)
        } finally {
            bufferSink?.close()
        }
    }

    fun readFile(file: File): String {
        var bufferSink: BufferedSource? = null
        try {
            bufferSink = Okio.buffer(Okio.source(file))
            val r = bufferSink.readUtf8()
            return r
        } catch (e: Exception) {
            MyLog.e(e)
        } finally {
            bufferSink?.close()
        }

        return ""
    }
}