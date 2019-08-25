package com.common.videocache

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
    private val saveFile = U.getAppInfoUtils().getSubDirFile("ori")
    private val fileNameGenerator = object : FileNameGenerator {
        override fun generate(url: String?): String {
            MyLog.d(TAG, "url=$url")
            return U.getMD5Utils().MD5_16(url) + "." + U.getFileUtils().getSuffixFromUrl(url, "m4a")
        }
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

    fun getProxyUrl(url: String, allowFromCache: Boolean): String {
        U.getHttpUtils().cancelDownload(url)
        return httpProxyCacheServer.getProxyUrl(url, allowFromCache)
    }

    val preCacheingSet = HashSet<String>()

    fun preCache(url: String) {
        MyLog.d(TAG, "preCache url=$url cacheingNum=${preCacheingSet.size}")
        if (preCacheingSet.size > 5) {
            return
        }
        if (preCacheingSet.contains(url)) {
            MyLog.d(TAG, "preCache url=$url 已经在下载 cancel")
            return
        }
        preCacheingSet.add(url)
        Observable.create<Unit> {
            val outFile = File(saveFile, fileNameGenerator.generate(url))
            val outFileTemp = File(saveFile, fileNameGenerator.generate(url) + ".temp")
            if (!outFile.exists() && !outFileTemp.exists()) {
                U.getHttpUtils().downloadFileSync(url, outFile, true, null, 1024 * 1024 * 2)
            }
            it.onComplete()
        }
                .subscribeOn(U.getThreadUtils().singleThreadPoll())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                }, {}, { preCacheingSet.remove(url) })

    }
}