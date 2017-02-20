package com.base.image.fresco.cache;

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

    private static MLCacheKeyFactory sInstance = null;

    protected MLCacheKeyFactory() {
    }

    public static synchronized DefaultCacheKeyFactory getInstance() {
        if (sInstance == null) {
            sInstance = new MLCacheKeyFactory();
        }

        return sInstance;
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

    @Override
    public CacheKey getEncodedCacheKey(ImageRequest request,@Nullable Object callerContext) {
        if (request.getSourceUri().toString().startsWith("http")) {
            Uri uri = this.getCacheKeySourceUri(request.getSourceUri());
            String url = uri.toString();
            String tmp = url;
            if (uri.getHost() != null) {
                tmp = url.replace(uri.getHost(), "host");
                if (isJpg(tmp)) {
                    tmp = tmp.replace(".jpg", "");
                    tmp += ".jpg";
                } else if (isJpeg(tmp)) {
                    tmp = tmp.replace(".jpeg", "");
                    tmp += ".jpeg";
                }
                return new SimpleCacheKey(tmp);
            }
        }
        return super.getEncodedCacheKey(request,callerContext);
    }

    @Override
    public CacheKey getBitmapCacheKey(ImageRequest request,@Nullable Object callerContext) {


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
            }
            return new BitmapMemoryCacheKey(tmp, null, request.getRotationOptions(), request.getImageDecodeOptions(), null, null,
                    callerContext);
        } else {
            return super.getBitmapCacheKey(request,callerContext);
        }
    }

}