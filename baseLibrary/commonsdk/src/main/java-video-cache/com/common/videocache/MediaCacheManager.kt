package com.common.videocache

import com.common.utils.U
import com.danikula.videocache.HttpProxyCacheServer

object MediaCacheManager {
    val httpProxyCacheServer = HttpProxyCacheServer.Builder(U.app())
            .cacheDirectory(U.getAppInfoUtils().getSubDirFile("ori"))
            .maxCacheSize(1024 * 1024 * 300)
            .fileNameGenerator {
                U.getMD5Utils().MD5_16(it)
            }
            //.headerInjector {  }
            .build()

    init {
//        httpProxyCacheServer.registerCacheListener(object :CacheListener{
//            override fun onCacheAvailable(cacheFile: File?, url: String?, percentsAvailable: Int) {
//            }
//        },"url")
    }

    fun getProxyUrl(url: String, allowFromCache: Boolean): String {
        return httpProxyCacheServer.getProxyUrl(url, allowFromCache)
    }
}