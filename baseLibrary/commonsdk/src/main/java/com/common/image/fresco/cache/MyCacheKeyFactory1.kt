package com.common.image.fresco.cache

import android.net.Uri
import android.text.TextUtils

import com.facebook.cache.common.CacheKey
import com.facebook.cache.common.SimpleCacheKey
import com.facebook.imagepipeline.cache.BitmapMemoryCacheKey
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory
import com.facebook.imagepipeline.request.ImageRequest

/**
 * Created by chengsimin on 16/9/5.
 */
object MyCacheKeyFactory1 : DefaultCacheKeyFactory() {

    internal fun isJpg(url: String): Boolean {
        var url = url
        val isJpg = false
        if (!TextUtils.isEmpty(url)) {
            url = url.toLowerCase()
            return url.contains(".jpg")
        }
        return isJpg
    }

    internal fun isJpeg(url: String): Boolean {
        var url = url
        val isJpeg = false
        if (!TextUtils.isEmpty(url)) {
            url = url.toLowerCase()
            return url.contains(".jpeg")
        }
        return isJpeg
    }

    internal fun isPng(url: String): Boolean {
        var url = url
        val isPng = false
        if (!TextUtils.isEmpty(url)) {
            url = url.toLowerCase()
            return url.contains(".png")
        }
        return isPng
    }

    internal fun isGif(url: String): Boolean {
        var url = url
        val isGif = false
        if (!TextUtils.isEmpty(url)) {
            url = url.toLowerCase()
            return url.contains(".gif")
        }
        return isGif
    }

    override fun getEncodedCacheKey(request: ImageRequest, callerContext: Any?): CacheKey? {
        var cacheKey: CacheKey? = null
        if (request.sourceUri.toString().startsWith("http")) {
            val uri = this.getCacheKeySourceUri(request.sourceUri)
            val url = uri.toString()
            var tmp = url
            if (uri.host != null) {
                tmp = url.replace(uri.host!!, "host").toLowerCase()
                if (isJpg(tmp)) {
                    tmp = tmp.replace(".jpg", "")
                    tmp += ".jpg"
                } else if (isJpeg(tmp)) {
                    tmp = tmp.replace(".jpeg", "")
                    tmp += ".jpeg"
                } else if (isPng(tmp)) {
                    tmp = tmp.replace(".png", "")
                    tmp += ".png"
                } else if (isGif(url)) {
                    tmp = tmp.replace(".gif", "")
                    tmp += ".gif"
                }
                cacheKey = SimpleCacheKey(tmp)
            }
        } else {
            cacheKey = super.getEncodedCacheKey(request, callerContext)
        }
        return cacheKey
    }

    override fun getBitmapCacheKey(request: ImageRequest, callerContext: Any?): CacheKey {
        var cacheKey: CacheKey? = null

        if (request.sourceUri.toString().startsWith("http")) {

            val uri = this.getCacheKeySourceUri(request.sourceUri)
            val url = uri.toString()
            var tmp = url.replace(uri.host!!, "host")
            if (isJpg(tmp)) {
                tmp = tmp.replace(".jpg", "")
                tmp += ".jpg"
            } else if (isJpeg(tmp)) {
                tmp = tmp.replace(".jpeg", "")
                tmp += ".jpeg"
            } else if (isPng(tmp)) {
                tmp = tmp.replace(".png", "")
                tmp += ".png"
            } else if (isGif(url)) {
                tmp = tmp.replace(".gif", "")
                tmp += ".gif"
            }
            cacheKey = BitmapMemoryCacheKey(tmp, request.resizeOptions, request.rotationOptions, request.imageDecodeOptions, null, null,
                    callerContext)
        } else {
            cacheKey = super.getBitmapCacheKey(request, callerContext)
        }
        return cacheKey!!
    }

}