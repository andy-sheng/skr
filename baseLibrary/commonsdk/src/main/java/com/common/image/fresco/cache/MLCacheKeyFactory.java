package com.common.image.fresco.cache;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.facebook.cache.common.CacheKey;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.imagepipeline.cache.BitmapMemoryCacheKey;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.request.ImageRequest;

/**
 * Created by chengsimin on 16/9/5.
 */
public class MLCacheKeyFactory extends DefaultCacheKeyFactory {
    static final String TAG = "MLCacheKeyFactory";
    private static MLCacheKeyFactory sInstance = null;

    private static class MLCacheKeyFactoryHolder {
        private static final MLCacheKeyFactory INSTANCE = new MLCacheKeyFactory();
    }

    private MLCacheKeyFactory() {

    }

    public static final MLCacheKeyFactory getInstance() {
        return MLCacheKeyFactoryHolder.INSTANCE;
    }

    boolean isJpg(String url) {
        boolean isJpg = false;
        if (!TextUtils.isEmpty(url)) {
            url = url.toLowerCase();
            return url.contains(".jpg");
        }
        return isJpg;
    }


    boolean isJpeg(String url) {
        boolean isJpeg = false;
        if (!TextUtils.isEmpty(url)) {
            url = url.toLowerCase();
            return url.contains(".jpeg");
        }
        return isJpeg;
    }

    boolean isPng(String url) {
        boolean isPng = false;
        if (!TextUtils.isEmpty(url)) {
            url = url.toLowerCase();
            return url.contains(".png");
        }
        return isPng;
    }

    boolean isGif(String url) {
        boolean isGif = false;
        if (!TextUtils.isEmpty(url)) {
            url = url.toLowerCase();
            return url.contains(".gif");
        }
        return isGif;
    }

    @Override
    public CacheKey getEncodedCacheKey(ImageRequest request, @Nullable Object callerContext) {
        CacheKey cacheKey = null;
        if (request.getSourceUri().toString().startsWith("http")) {
            Uri uri = this.getCacheKeySourceUri(request.getSourceUri());
            String url = uri.toString();
            String tmp = url;
            if (uri.getHost() != null) {
                tmp = url.replace(uri.getHost(), "host").toLowerCase();
                if (isJpg(tmp)) {
                    tmp = tmp.replace(".jpg", "");
                    tmp += ".jpg";
                } else if (isJpeg(tmp)) {
                    tmp = tmp.replace(".jpeg", "");
                    tmp += ".jpeg";
                } else if (isPng(tmp)) {
                    tmp = tmp.replace(".png", "");
                    tmp += ".png";
                } else if (isGif(url)) {
                    tmp = tmp.replace(".gif", "");
                    tmp += ".gif";
                }
                cacheKey = new SimpleCacheKey(tmp);
            }
        } else {
            cacheKey = super.getEncodedCacheKey(request, callerContext);
        }
        return cacheKey;
    }

    @Override
    public CacheKey getBitmapCacheKey(ImageRequest request, @Nullable Object callerContext) {
        CacheKey cacheKey = null;

        if (request.getSourceUri().toString().startsWith("http")) {

            Uri uri = this.getCacheKeySourceUri(request.getSourceUri());
            String url = uri.toString();
            String tmp = url.replace(uri.getHost(), "host");
            if (isJpg(tmp)) {
                tmp = tmp.replace(".jpg", "");
                tmp += ".jpg";
            } else if (isJpeg(tmp)) {
                tmp = tmp.replace(".jpeg", "");
                tmp += ".jpeg";
            } else if (isPng(tmp)) {
                tmp = tmp.replace(".png", "");
                tmp += ".png";
            } else if (isGif(url)) {
                tmp = tmp.replace(".gif", "");
                tmp += ".gif";
            }
            cacheKey = new BitmapMemoryCacheKey(tmp, request.getResizeOptions(), request.getRotationOptions(), request.getImageDecodeOptions(), null, null,
                    callerContext);
        } else {
            cacheKey = super.getBitmapCacheKey(request, callerContext);
        }
        return cacheKey;
    }

}