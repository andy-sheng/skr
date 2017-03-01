package com.wali.live.fresco;

import android.content.Context;
import android.os.Environment;

import com.base.global.GlobalData;
import com.base.image.fresco.cache.MLCacheKeyFactory;
import com.base.image.fresco.config.ConfigConstants;
import com.base.image.fresco.log.LiveFrescoDelegate;
import com.base.utils.Constants;
import com.base.utils.sdcard.SDCardUtils;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.common.logging.FLog;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.listener.RequestListener;
import com.facebook.imagepipeline.listener.RequestLoggingListener;
import com.squareup.okhttp.OkHttpClient;
import com.wali.live.network.CustomOkHttpNetworkFetcher;
import com.wali.live.utils.HttpUtils;
import com.wali.live.utils.UserAgentInterceptor;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lan on 16/7/20.
 */
public class FrescoManager {
    public static void initFresco(Context context) {
        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(context)
                .setBaseDirectoryPath(new File(Environment.getExternalStorageDirectory(), SDCardUtils.FRESCO_DIR_PATH))
                .setBaseDirectoryName("v1")
                .setMaxCacheSize(80 * ByteConstants.MB)
                .setMaxCacheSizeOnLowDiskSpace(10 * ByteConstants.MB)
                .setMaxCacheSizeOnVeryLowDiskSpace(5 * ByteConstants.MB)
                .setVersion(1)
                .build();

        Set<RequestListener> requestListeners = new HashSet();
        requestListeners.add(new RequestLoggingListener());

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.networkInterceptors().add(new UserAgentInterceptor(HttpUtils.buildUserAgent(GlobalData.app())));
        ImagePipelineConfig.Builder imagePipelineConfig = ImagePipelineConfig.newBuilder(context)
                .setNetworkFetcher(new CustomOkHttpNetworkFetcher(okHttpClient))
                .setMainDiskCacheConfig(diskCacheConfig)
                .setSmallImageDiskCacheConfig(diskCacheConfig)
                .setRequestListeners(requestListeners)
                .setDownsampleEnabled(true);
        configureCaches(imagePipelineConfig, context);
        configureLoggingListeners(imagePipelineConfig);

        Fresco.initialize(context, imagePipelineConfig.build());
        FLog.setLoggingDelegate(LiveFrescoDelegate.getInstance());
        FLog.setMinimumLoggingLevel(Constants.isDebugOrTestBuild ? FLog.VERBOSE : FLog.WARN);
    }

    private static void configureCaches(ImagePipelineConfig.Builder configBuilder, Context context) {
        final MemoryCacheParams bitmapCacheParams = new MemoryCacheParams(
                ConfigConstants.MAX_MEMORY_CACHE_SIZE,          // Max total size of elements in the cache
                Integer.MAX_VALUE,                              // Max entries in the cache
                ConfigConstants.MAX_MEMORY_CACHE_SIZE,          // Max total size of elements in eviction queue
                Integer.MAX_VALUE,                              // Max length of eviction queue
                Integer.MAX_VALUE);                             // Max cache entry size
        final MemoryCacheParams encodeBitmapCacheParams = new MemoryCacheParams(
                ConfigConstants.MAX_MEMORY_CACHE_SIZE,          // Max total size of elements in the cache
                Integer.MAX_VALUE,                              // Max entries in the cache
                ConfigConstants.MAX_MEMORY_CACHE_SIZE,          // Max total size of elements in eviction queue
                Integer.MAX_VALUE,                              // Max length of eviction queue
                Integer.MAX_VALUE);                             // Max cache entry size
        configBuilder.setBitmapMemoryCacheParamsSupplier(
                new Supplier<MemoryCacheParams>() {
                    public MemoryCacheParams get() {
                        return bitmapCacheParams;
                    }
                })
                .setEncodedMemoryCacheParamsSupplier(new Supplier<MemoryCacheParams>() {
                    @Override
                    public MemoryCacheParams get() {
                        return encodeBitmapCacheParams;
                    }
                })
                .setCacheKeyFactory(MLCacheKeyFactory.getInstance());
    }

    private static void configureLoggingListeners(ImagePipelineConfig.Builder configBuilder) {
        Set<RequestListener> requestListeners = new HashSet();
        requestListeners.add(new RequestLoggingListener());
        configBuilder.setRequestListeners(requestListeners);
    }
}
