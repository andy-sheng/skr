package com.common.image.fresco;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.common.base.BuildConfig;
import com.common.http.CustomOkHttpNetworkFetcher;
import com.common.http.UserAgentInterceptor;
import com.common.image.fresco.cache.MLCacheKeyFactory;
import com.common.image.fresco.log.FrescoLogDelegate;
import com.common.log.MyLog;
import com.common.utils.U;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.common.logging.FLog;
import com.facebook.common.memory.MemoryTrimType;
import com.facebook.common.memory.MemoryTrimmable;
import com.facebook.common.memory.MemoryTrimmableRegistry;
import com.facebook.common.memory.NoOpMemoryTrimmableRegistry;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp.OkHttpNetworkFetcher;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.listener.RequestListener;
import com.facebook.imagepipeline.listener.RequestLoggingListener;
import com.facebook.imagepipeline.producers.HttpUrlConnectionNetworkFetcher;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by lan on 16-1-11.
 */
public class FrescoInitManager {

    public final static String TAG = "FrescoInitManager";

    public final static String FRESCO_DIR_PATH = String.format("/%s/fresco", U.getAppInfoUtils().getAppName());


    public static void initFresco(Context context) {
        MyLog.d(TAG,"initFresco FRESCO_DIR_PATH=" + FRESCO_DIR_PATH);
        // 配置文件目录
        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(context)
                .setBaseDirectoryPath(new File(Environment.getExternalStorageDirectory(), FRESCO_DIR_PATH))
                .setBaseDirectoryName("photov2")
                .setVersion(1)
                .setMaxCacheSize(500 * ByteConstants.MB)
                .build();

        DiskCacheConfig smallDiskCacheConfig = DiskCacheConfig.newBuilder(context)
                .setBaseDirectoryPath(new File(Environment.getExternalStorageDirectory(), FRESCO_DIR_PATH))
                .setBaseDirectoryName("thumbnail")
                .setVersion(1)
                .setMaxCacheSize(500 * ByteConstants.MB)
                .build();
        // 当内存紧张时采取的措施
        MemoryTrimmableRegistry memoryTrimmableRegistry = NoOpMemoryTrimmableRegistry.getInstance();
        memoryTrimmableRegistry.registerMemoryTrimmable(new MemoryTrimmable() {
            @Override
            public void trim(MemoryTrimType trimType) {
                final double suggestedTrimRatio = trimType.getSuggestedTrimRatio();
                MyLog.w(String.format("Fresco onCreate suggestedTrimRatio : %d", suggestedTrimRatio));

                if (MemoryTrimType.OnCloseToDalvikHeapLimit.getSuggestedTrimRatio() == suggestedTrimRatio
                        || MemoryTrimType.OnSystemLowMemoryWhileAppInBackground.getSuggestedTrimRatio() == suggestedTrimRatio
                        || MemoryTrimType.OnSystemLowMemoryWhileAppInForeground.getSuggestedTrimRatio() == suggestedTrimRatio
                        ) {
                    // 清除内存缓存
                    Fresco.getImagePipeline().clearMemoryCaches();
                }
            }
        });
        OkHttpClient okHttpClient = new OkHttpClient();
        Set<RequestListener> requestListeners = new HashSet<>();
        requestListeners.add(new RequestLoggingListener());
        okHttpClient.networkInterceptors().add(new UserAgentInterceptor(U.getHttpUtils().buildUserAgent()));
        final File baseDir = context.getCacheDir();
        if (baseDir != null) {
            final File cacheDir = new File(baseDir, "HttpResponseCache");
            okHttpClient.setCache(new com.squareup.okhttp.Cache(cacheDir, 10 * ByteConstants.MB));
        }
        okHttpClient.setReadTimeout(20 * 1000, TimeUnit.MILLISECONDS);
        okHttpClient.setConnectTimeout(15 * 1000, TimeUnit.MILLISECONDS);

        ImagePipelineConfig.Builder imagePipelineConfig = ImagePipelineConfig.newBuilder(context)
                .setNetworkFetcher(new CustomOkHttpNetworkFetcher(okHttpClient))
                .setMainDiskCacheConfig(diskCacheConfig)
                .setSmallImageDiskCacheConfig(smallDiskCacheConfig)
                .setMemoryTrimmableRegistry(memoryTrimmableRegistry)
                .setRequestListeners(requestListeners)
                .setDownsampleEnabled(true);
        configureCaches(imagePipelineConfig, context);
        configureLoggingListeners(imagePipelineConfig);
        Fresco.initialize(context, imagePipelineConfig.build());

        FLog.setLoggingDelegate(new FrescoLogDelegate("FrescoLogDelegate"));
        FLog.setMinimumLoggingLevel(BuildConfig.DEBUG ? FLog.WARN : FLog.ERROR);
    }


    /**
     * Configures disk and memory cache not to exceed common limits
     */
    private static void configureCaches(ImagePipelineConfig.Builder configBuilder, Context context) {
        MyLog.w("InitManager", "configureCaches MAX_MEMORY_CACHE_SIZE=" + getMaxCacheSize()
        );
        final MemoryCacheParams bitmapCacheParams = getMemoryCacheParams();          // Max cache entry size
        configBuilder
                .setBitmapMemoryCacheParamsSupplier(
                        new Supplier<MemoryCacheParams>() {
                            public MemoryCacheParams get() {
                                return bitmapCacheParams;
                            }
                        })
                .setCacheKeyFactory(MLCacheKeyFactory.getInstance());
    }

    static MemoryCacheParams getMemoryCacheParams() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new MemoryCacheParams(getMaxCacheSize(), // 内存缓存中总图片的最大大小,以字节为单位。
                    56,                                     // 内存缓存中图片的最大数量。
                    Integer.MAX_VALUE,                      // 内存缓存中准备清除但尚未被删除的总图片的最大大小,以字节为单位。
                    Integer.MAX_VALUE,                      // 内存缓存中准备清除的总图片的最大数量。
                    Integer.MAX_VALUE);                     // 内存缓存中单个图片的最大大小。
        } else {
            return new MemoryCacheParams(
                    getMaxCacheSize(),
                    256,
                    Integer.MAX_VALUE,
                    Integer.MAX_VALUE,
                    Integer.MAX_VALUE);
        }
    }

    private static int getMaxCacheSize() {
        ActivityManager activityManager = (ActivityManager) U.app().getSystemService(Context.ACTIVITY_SERVICE);
        final int maxMemory = Math.min(activityManager.getMemoryClass() * ByteConstants.MB, Integer.MAX_VALUE);
        MyLog.w(TAG, String.format("Fresco Max memory [%d] MB", (maxMemory / ByteConstants.MB)));
        if (maxMemory < 32 * ByteConstants.MB) {
            return 4 * ByteConstants.MB;
        } else if (maxMemory < 64 * ByteConstants.MB) {
            return 6 * ByteConstants.MB;
        } else {
            // We don't want to use more ashmem on Gingerbread for now, since it doesn't respond well to
            // native memory pressure (doesn't throw exceptions, crashes app, crashes phone)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                return 8 * ByteConstants.MB;
            } else {
                return maxMemory / 10;
            }
        }
    }

    private static void configureLoggingListeners(ImagePipelineConfig.Builder configBuilder) {
        Set<RequestListener> requestListeners = new HashSet<RequestListener>();
        requestListeners.add(new RequestLoggingListener());
        configBuilder.setRequestListeners(requestListeners);
    }

}