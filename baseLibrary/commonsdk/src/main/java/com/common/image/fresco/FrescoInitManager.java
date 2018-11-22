package com.common.image.fresco;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;

import com.common.base.BuildConfig;
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
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
import com.facebook.imagepipeline.listener.RequestListener;
import com.facebook.imagepipeline.listener.RequestLoggingListener;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lan on 16-1-11.
 */
public class FrescoInitManager {
    public final static String TAG = "FrescoInitManager";


    public static void initFresco(Context context) {
        File saveDir = new File(U.getAppInfoUtils().getMainDir(), "fresco/");
        MyLog.d(TAG, "initFresco FRESCO_DIR_PATH=" + saveDir.getAbsolutePath());
        // 配置文件目录
        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(context)
                .setBaseDirectoryPath(saveDir)
                .setBaseDirectoryName("photov2")
                .setVersion(1)
                .setMaxCacheSize(500 * ByteConstants.MB)
                .build();

        // 小文件的目录
        DiskCacheConfig smallDiskCacheConfig = DiskCacheConfig.newBuilder(context)
                .setBaseDirectoryPath(saveDir)
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
        Set<RequestListener> requestListeners = new HashSet<>();
        requestListeners.add(new RequestLoggingListener());

//        // 配置自定义的网络请求处理
//        OkHttpClient okHttpClient = new OkHttpClient();
//
//        //自定义一个拦截器
//        okHttpClient.networkInterceptors().add(new UserAgentInterceptor(U.getHttpUtils().buildUserAgent()));
//
//        final File baseDir = context.getCacheDir();
//        if (baseDir != null) {
//            final File cacheDir = new File(baseDir, "HttpResponseCache");
//            okHttpClient.setCache(new com.squareup.okhttp.Cache(cacheDir, 10 * ByteConstants.MB));
//        }
//
//        okHttpClient.setReadTimeout(20 * 1000, TimeUnit.MILLISECONDS);
//        okHttpClient.setConnectTimeout(15 * 1000, TimeUnit.MILLISECONDS);

        ImagePipelineConfig.Builder imagePipelineConfig = ImagePipelineConfig.newBuilder(context)
                //测试test时发现，自己实现成功率还没系统实现的成功率高，
                // 什么域名预解析没什么用，可能对外部的图片地址没什么用
//                .setNetworkFetcher(new CustomOkHttpNetworkFetcher(okHttpClient))
                .setMainDiskCacheConfig(diskCacheConfig)
                .setSmallImageDiskCacheConfig(smallDiskCacheConfig)
                .setMemoryTrimmableRegistry(memoryTrimmableRegistry)
                .setRequestListeners(requestListeners)
                // 在RGB_565的条件下过滤alpha通道，图片消耗内存量会降低，进一步降低OOM的风险
                // 低端机型可以开启这个
                .setBitmapsConfig(Bitmap.Config.RGB_565)
                //在设置ImageRequest的时候允许其进行resize处理，减少内存消耗，也同样起到降低OOM的风险
                .setResizeAndRotateEnabledForNetwork(true)
                // fresco仅支持文件类型为JPEG的网络图片，因为本地图片均一次性解码完成，
                // 所以本地图片不需要使用渐进式。在fresco中，你可以设置一个清晰度标准，使其在达到这个标准之前一直以占位图显示
                .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
                //必须和ImageRequest的ResizeOptions一起使用，也是起到降低OOM的风险
                .setDownsampleEnabled(true);
        configureCaches(imagePipelineConfig, context);
        configureLoggingListeners(imagePipelineConfig);
        Fresco.initialize(context, imagePipelineConfig.build());

        FLog.setLoggingDelegate(new FrescoLogDelegate("FrescoLogDelegate"));
        FLog.setMinimumLoggingLevel(BuildConfig.DEBUG ? FLog.ERROR : FLog.ERROR);
    }


    /**
     * Configures disk and memory cache not to exceed common limits
     */
    private static void configureCaches(ImagePipelineConfig.Builder configBuilder, Context context) {
        MyLog.w("InitManager", "configureCaches MAX_MEMORY_CACHE_SIZE=" + getMaxCacheSize());
        final MemoryCacheParams bitmapCacheParams = getMemoryCacheParams();          // Max cache entry size

        configBuilder.setBitmapMemoryCacheParamsSupplier(
                new Supplier<MemoryCacheParams>() {
                    public MemoryCacheParams get() {
                        return bitmapCacheParams;
                    }
                })
//                .setEncodedMemoryCacheParamsSupplier(new Supplier<MemoryCacheParams>() {
//                    @Override
//                    public MemoryCacheParams get() {
//                        return encodeBitmapCacheParams;
//                    }
//                })
                .setCacheKeyFactory(MLCacheKeyFactory.getInstance());
    }

    static MemoryCacheParams getMemoryCacheParams() {
        int maxCacheSize = getMaxCacheSize();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new MemoryCacheParams(maxCacheSize, // 内存缓存中总图片的最大大小,以字节为单位。
                    56,                                     // 内存缓存中图片的最大数量。
                    maxCacheSize,                      // 内存缓存中准备清除但尚未被删除的总图片的最大大小,以字节为单位。
                    Integer.MAX_VALUE,                      // 内存缓存中准备清除的总图片的最大数量。
                    Integer.MAX_VALUE);                     // 内存缓存中单个图片的最大大小。
        } else {
            return new MemoryCacheParams(
                    maxCacheSize,
                    256,
                    maxCacheSize,
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