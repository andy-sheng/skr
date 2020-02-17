package com.common.videocache

import android.text.TextUtils
import com.common.log.MyLog
import com.common.utils.U
import com.danikula.videocache.HttpProxyCacheServer
import com.danikula.videocache.file.FileNameGenerator
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import java.io.File

object MediaCacheManager {
    private val TAG = "MediaCacheManager"
    private val saveFile = U.getAppInfoUtils().getSubDirFile("acc2")
    private val fileNameGenerator = FileNameGenerator { url ->
        MyLog.d(TAG, "url=$url")
        var url2 = getOriginCdnUrl(url)
        U.getMD5Utils().MD5_16(url2) + "." + U.getFileUtils().getSuffixFromUrl(url2, "m4a")
    }

    private val httpProxyCacheServer = HttpProxyCacheServer.Builder(U.app())
            .cacheDirectory(saveFile)
            .maxCacheSize(1024 * 1024 * 300)
            .fileNameGenerator(fileNameGenerator)
            //.headerInjector {  }
            .build()

    init {
//        httpProxyCacheServer.registerCacheListener(object :CacheListener{
//            override fun onCacheAvailable(cacheFile: File?, url: String?, percentsAvailable: Int) {
//            }
//        },"url")
    }

    fun getOriginCdnUrl(url: String?): String? {
        if (url?.matches(Regex("http(s)?://[a-z]+-static-[0-9]+\\.inframe\\.mobi/.*")) == true) {
            return url.replace(Regex("-static-[0-9]+"), "-static")
        }
        return url
    }

    fun getProxyUrl(url: String, allowFromCache: Boolean): String {
        cancelPreCache(url)
        return httpProxyCacheServer.getProxyUrl(url, allowFromCache)
    }

    private val preCachingSet = HashSet<String>()

    fun preCache(url: String, maxSaveSize: Int=-1) {
        MyLog.d(TAG, "preCache url=$url size=$maxSaveSize cacheingNum=${preCachingSet.size}")

        synchronized(preCachingSet) {
            if (preCachingSet.contains(url)) {
                MyLog.d(TAG, "preCache url=$url 已经在下载 cancel")
                return
            }
            preCachingSet.add(url)
        }
        Observable.create<Unit> {
            val outFile = File(saveFile, fileNameGenerator.generate(url))
            val outFileTemp = File(saveFile, fileNameGenerator.generate(url) + ".temp")
            if (!outFile.exists() && !outFileTemp.exists()) {
                val proxyUrl = httpProxyCacheServer.getProxyUrl(url, true)
                val isNotCanceled: Boolean
                synchronized(preCachingSet) {
                    isNotCanceled = preCachingSet .contains(url)
                }
                if (isNotCanceled && !proxyUrl.startsWith("file")) {
                    U.getHttpUtils().downloadFileSync(proxyUrl, null, true, null, maxSaveSize)
                }
            }
            it.onComplete()
        }
                .subscribeOn(U.getThreadUtils().singleThreadPoll())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                }, {}, {
                    MyLog.d(TAG, "preCache complete, remove url from preCache: $url")
                    synchronized(preCachingSet) {
                        preCachingSet.remove(url)
                    }
                })

    }

    fun cancelPreCache(url: String?=null) {
        MyLog.d(TAG, "cancelPreCache: $url")
        if (url != null) {
            synchronized(preCachingSet) {
                U.getHttpUtils().cancelDownload(url)
                preCachingSet.remove(url)
            }
        } else {
            synchronized(preCachingSet) {
                for (i in preCachingSet) {
                    U.getHttpUtils().cancelDownload(i)
                }
                preCachingSet.clear()
            }
        }
    }
}