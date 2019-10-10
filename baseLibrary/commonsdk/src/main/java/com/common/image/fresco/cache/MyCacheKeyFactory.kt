package com.common.image.fresco.cache

import android.net.Uri
import com.common.log.MyLog
import com.facebook.cache.common.CacheKey
import com.facebook.cache.common.SimpleCacheKey
import com.facebook.imagepipeline.cache.BitmapMemoryCacheKey
import com.facebook.imagepipeline.cache.CacheKeyFactory
import com.facebook.imagepipeline.request.ImageRequest

/**
 * Created by chengsimin on 16/9/5.
 */
object MyCacheKeyFactory : CacheKeyFactory {

    override fun getBitmapCacheKey(request: ImageRequest, callerContext: Any?): CacheKey {
        return BitmapMemoryCacheKey(
                getCacheKeySourceUri(request.sourceUri).toString(),
                request.resizeOptions,
                request.rotationOptions,
                request.imageDecodeOptions,
                null, null,
                callerContext)
    }

    override fun getPostprocessedBitmapCacheKey(request: ImageRequest, callerContext: Any?): CacheKey {
        val postprocessor = request.postprocessor
        val postprocessorCacheKey: CacheKey?
        val postprocessorName: String?
        if (postprocessor != null) {
            postprocessorCacheKey = postprocessor.postprocessorCacheKey
            postprocessorName = postprocessor.javaClass.name
        } else {
            postprocessorCacheKey = null
            postprocessorName = null
        }
        return BitmapMemoryCacheKey(
                getCacheKeySourceUri(request.sourceUri).toString(),
                request.resizeOptions,
                request.rotationOptions,
                request.imageDecodeOptions,
                postprocessorCacheKey,
                postprocessorName,
                callerContext)
    }

    override fun getEncodedCacheKey(request: ImageRequest, callerContext: Any?): CacheKey {
        return getEncodedCacheKey(request, request.sourceUri, callerContext)
    }

    override fun getEncodedCacheKey(
            request: ImageRequest,
            sourceUri: Uri,
            callerContext: Any?): CacheKey {
        return SimpleCacheKey(getCacheKeySourceUri(sourceUri).toString())
    }

    /**
     * @return a [Uri] that unambiguously indicates the source of the image.
     */
    open fun getCacheKeySourceUri(sourceUri: Uri): Uri {
        if (sourceUri.path.endsWith(".gif")) {
            //如果是gif，则忽略后面的参数
            MyLog.d("MyCacheKeyFactory", "getCacheKeySourceUri sourceUri = $sourceUri")
        }
        return sourceUri
    }


}