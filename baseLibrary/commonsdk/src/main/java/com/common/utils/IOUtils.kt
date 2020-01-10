package com.common.utils

import android.text.TextUtils
import com.common.log.MyLog
import okio.BufferedSink
import okio.BufferedSource
import okio.Okio
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset


class IOUtils {
    fun writeFile(content: String, file: File) {
        if (TextUtils.isEmpty(content)) {
            return
        }
        var bufferSink: BufferedSink? = null
        try {
            if (!file.parentFile.exists()) {
                file.getParentFile().mkdirs()
            }

            if (!file.exists()) {
                file.createNewFile()
            }
            bufferSink = Okio.buffer(Okio.sink(file))
            val byte = U.getBase64Utils().decode(content)
            bufferSink.write(byte)
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

    fun copy(src: File, dst: File) {
        val fin = FileInputStream(src)
        fin.use { fin ->
            val out = FileOutputStream(dst)
            out.use { out ->
                // Transfer bytes from in to out
                val buf = ByteArray(1024)
                var len = fin.read(buf)
                while (len > 0) {
                    out.write(buf, 0, len)
                    len = fin.read(buf)
                }
            }
        }
    }
}