package com.common.utils

import android.text.TextUtils
import okio.Okio
import java.io.File
import java.nio.charset.Charset

class IOUtils {
    fun writeFile(content: String, file: File) {
        if(TextUtils.isEmpty(content)){
            return
        }
        val bufferSink = Okio.buffer(Okio.sink(file))
        bufferSink.writeString(content, Charset.forName("utf-8"))
        bufferSink.close()
    }

    fun readFile(file: File):String {
        val bufferSink = Okio.buffer(Okio.source(file))
        val r = bufferSink.readUtf8()
        bufferSink.close()
        return r
    }
}