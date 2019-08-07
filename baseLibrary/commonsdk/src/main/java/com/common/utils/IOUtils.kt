package com.common.utils

import android.text.TextUtils
import com.common.log.MyLog
import okio.Okio
import java.io.File
import java.nio.charset.Charset

class IOUtils {
    fun writeFile(content: String, file: File) {
        if (TextUtils.isEmpty(content)) {
            return
        }
        try {
            val bufferSink = Okio.buffer(Okio.sink(file))
            bufferSink.writeString(content, Charset.forName("utf-8"))
            bufferSink.close()
        } catch (e: Exception) {
            MyLog.e(e)
        }
    }

    fun readFile(file: File): String {
        try {
            val bufferSink = Okio.buffer(Okio.source(file))
            val r = bufferSink.readUtf8()
            bufferSink.close()
            return r
        } catch (e: Exception) {
            MyLog.e(e)
        }
        return ""
    }
}