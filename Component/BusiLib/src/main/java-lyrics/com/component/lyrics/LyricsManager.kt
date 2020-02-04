package com.component.lyrics

import android.text.TextUtils
import com.common.core.crash.IgnoreException
import com.common.core.global.ResServerApi
import com.common.log.MyLog
import com.common.rx.RxRetryAssist
import com.common.rxretrofit.ApiManager
import com.common.utils.U
import com.component.lyrics.exception.LyricLoadFailedException
import com.component.lyrics.utils.SongResUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.IOException

object LyricsManager {

    val TAG = "LyricsManager"
    internal var mResServerApi = ApiManager.getInstance().createService(ResServerApi::class.java)

    fun loadStandardLyric(url: String?): Observable<LyricsReader> = loadStandardLyric(url, null)

    /**
     * shift 偏移多少秒,如果==-1 就说明偏移 第一句 的开始时间 那么长时间
     */
    fun loadStandardLyric(url: String?, shift: Int): Observable<LyricsReader> = loadStandardLyric(url) {
        if (shift != 0) {
            val it = it.lrcLineInfos.entries.iterator()
            var num = 0
            var sh = shift
            while (it.hasNext()) {
                val entry = it.next()
                if (shift == -1 && num == 0) {
                    sh = entry.value.startTime
                }
                entry.value.startTime = entry.value.startTime - sh
                entry.value.endTime = entry.value.endTime - sh
                num++
            }
        }
    }

    /**
     * 加载标准歌词 url
     * 注意目前 逐字 一般为 zrce 歌词
     * 如果歌词已经是解密过的 目前为 zrce2 歌词
     * 如果服务器下发的带时间戳的歌词不为这两种歌词，大概率是他们的bug
     * @param url
     * @return
     */
    fun loadStandardLyric(url: String?, processReader: ((reader: LyricsReader) -> Unit)?): Observable<LyricsReader> {
        var b = System.currentTimeMillis()
        return Observable.create(ObservableOnSubscribe<File> { emitter ->
            if (TextUtils.isEmpty(url)) {
                if (MyLog.isDebugLogOpen()) {
                    U.getToastUtil().showShort("歌词 url==null")
                }
                emitter.onComplete()
                return@ObservableOnSubscribe
            }
            val newName = File(SongResUtils.createLyricFileName(url))
            if (newName.exists() && newName.isFile) {
                emitter.onNext(newName)
                emitter.onComplete()
                return@ObservableOnSubscribe
            }
            var isSuccess = false
            isSuccess = U.getHttpUtils().downloadFileSync(url, newName, true, null, -1, 2 * 1000, 3 * 1000)
            if (isSuccess) {
                emitter.onNext(newName)
            } else {
                MyLog.w(TAG, "使用服务器代理下载")
                val call = mResServerApi.getLyricByUrl(url)
                try {
                    val response = call.execute()
                    val jsonObject = response.body()
                    if (jsonObject != null) {
                        MyLog.w(TAG, "body=$jsonObject")
                        val content = jsonObject.getString("body")
                        U.getIOUtils().writeFile(content, newName)
                        emitter.onNext(newName)
                    } else {
                        emitter.onError(IgnoreException("代理下载，歌词为空"))
                    }
                } catch (e: IOException) {
                    if (MyLog.isDebugLogOpen()) {
                        U.getToastUtil().showShort("歌词文件下载失败 url=$url")
                    }
                    emitter.onError(IgnoreException("代理下载失败"))
                    return@ObservableOnSubscribe
                }
            }
            emitter.onComplete()
        }).map { file ->
            val lyricsReader = LyricsReader()
            try {
                lyricsReader.loadLrc(file)
                processReader?.invoke(lyricsReader)
                if (MyLog.isDebugLogOpen()) {
                    if (lyricsReader.lrcLineInfos.isEmpty()) {
                        U.getToastUtil().showLong("时间戳歌词文件解析后内容为空 url=$url")
                    } else {
//                        lyricsReader.lrcLineInfos.iterator().forEach {
//                            it.value.lineLyrics = "${it.value.startTime}:${it.value.lineLyrics}"
//                        }
                    }
                }
            } catch (e: Exception) {
                MyLog.e("LyricsManager", e)
            }
            MyLog.w(TAG, "stand歌词parse完毕,耗时${System.currentTimeMillis() - b}")
            if (lyricsReader.lrcLineInfos == null || lyricsReader.lrcLineInfos.size == 0) {
                //数据有问题，需要删除文件
                MyLog.w(TAG, "${url} 数据有问题，删除。此文件的md5结果是 ${U.getMD5Utils().getFileMD5(file)}")
                if (file.exists()) {
                    file.delete()
                    MyLog.w(TAG, "删除成功")
                }
                throw LyricLoadFailedException("LyricLoadFailedException exception")
            }
            lyricsReader
        }
                .subscribeOn(U.getThreadUtils().urgentIO())
                .retryWhen(RxRetryAssist(5, ""))
                .observeOn(AndroidSchedulers.mainThread())
    }


    /**
     * 加载一唱到底普通文本歌词
     *
     * @param url
     * @return
     */
    fun loadGrabPlainLyric(url: String?): Observable<String> {
        MyLog.w(TAG, "loadGrabPlainLyric url=$url")
        return Observable.create(ObservableOnSubscribe<String> { emitter ->
            if (TextUtils.isEmpty(url)) {
                if (MyLog.isDebugLogOpen()) {
                    U.getToastUtil().showShort("歌词 url==null")
                }
                emitter.onComplete()
                return@ObservableOnSubscribe
            }
            val file = SongResUtils.getGrabLyricFileByUrl(url)
            if (file == null || !file.exists()) {
                var isSuccess = false
                isSuccess = U.getHttpUtils().downloadFileSync(url, file, true, null, -1, 2 * 1000, 3 * 1000)
                if (isSuccess) {
                    val content = U.getIOUtils().readFile(file!!)
                    if (!TextUtils.isEmpty(content)) {
                        emitter.onNext(content)
                    } else {
                        emitter.onNext("歌词buffer读取失败")
                    }
                } else {
                    MyLog.w(TAG, "使用服务器代理下载")
                    val call = mResServerApi.getLyricByUrl(url)
                    try {
                        val response = call.execute()
                        val jsonObject = response.body()
                        if (jsonObject != null) {
                            MyLog.d(TAG, "body=$jsonObject")
                            val content = jsonObject.getString("body")
                            U.getIOUtils().writeFile(content, file!!)
                            if (!TextUtils.isEmpty(content)) {
                                emitter.onNext(content)
                            }
                        } else {
                            emitter.onError(IgnoreException("代理下载，歌词为空"))
                        }
                    } catch (e: IOException) {
                        if (MyLog.isDebugLogOpen()) {
                            U.getToastUtil().showShort("歌词文件下载失败 url=$url")
                        }
                        emitter.onError(IgnoreException("代理下载失败"))
                        return@ObservableOnSubscribe
                    }

                }
            } else {
                MyLog.w(TAG, "playLyric is exist")
                val content = U.getIOUtils().readFile(file)
                emitter.onNext(content)
            }
            emitter.onComplete()
        })
                .subscribeOn(Schedulers.io())
                .retryWhen(RxRetryAssist(5, ""))
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun createZrce2ByReader(zrce2Reader: LyricsReader?): String {
        val sb = StringBuilder()
        zrce2Reader?.lrcLineInfos?.forEach { it ->
            MyLog.d(TAG, "it.key = ${it.key}")
            MyLog.d(TAG, "it.value = ${it.value}")
            val info = it.value
            sb.append("[${info.startTime},${info.endTime - info.startTime}]")
            var wordBeginTs = 0
            val customLine = info.lineLyrics
            var wordBeginIndex = 0
            for (i in 0 until info.wordsDisInterval.size) {
                if (i < (info?.lyricsWords?.size ?: 0)) {
                    var word = info.lyricsWords[i]
                    word = customLine.substring(wordBeginIndex, wordBeginIndex + word.length)
                    wordBeginIndex += word.length
                    val wordDuration = info.wordsDisInterval[i]
                    sb.append("<${wordBeginTs},${wordDuration},0>").append(word)
                    wordBeginTs += wordDuration
                }
            }
            sb.append("\n")
        }
        return sb.toString()
    }
}
