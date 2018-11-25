//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

import io.rong.imageloader.cache.disc.DiskCache;
import io.rong.imageloader.cache.disc.naming.FileNameGenerator;
import io.rong.imageloader.cache.memory.MemoryCache;
import io.rong.imageloader.cache.memory.impl.FuzzyKeyMemoryCache;
import io.rong.imageloader.core.DefaultConfigurationFactory;
import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.assist.FlushedInputStream;
import io.rong.imageloader.core.assist.ImageSize;
import io.rong.imageloader.core.assist.QueueProcessingType;
import io.rong.imageloader.core.decode.ImageDecoder;
import io.rong.imageloader.core.download.ImageDownloader;
import io.rong.imageloader.core.process.BitmapProcessor;
import io.rong.imageloader.utils.L;
import io.rong.imageloader.utils.MemoryCacheUtils;

public final class ImageLoaderConfiguration {
  final Resources resources;
  final int maxImageWidthForMemoryCache;
  final int maxImageHeightForMemoryCache;
  final int maxImageWidthForDiskCache;
  final int maxImageHeightForDiskCache;
  final BitmapProcessor processorForDiskCache;
  final Executor taskExecutor;
  final Executor taskExecutorForCachedImages;
  final boolean customExecutor;
  final boolean customExecutorForCachedImages;
  final int threadPoolSize;
  final int threadPriority;
  final QueueProcessingType tasksProcessingType;
  final MemoryCache memoryCache;
  final DiskCache diskCache;
  final ImageDownloader downloader;
  final ImageDecoder decoder;
  final DisplayImageOptions defaultDisplayImageOptions;
  final ImageDownloader networkDeniedDownloader;
  final ImageDownloader slowNetworkDownloader;

  private ImageLoaderConfiguration(io.rong.imageloader.core.ImageLoaderConfiguration.Builder builder) {
    this.resources = builder.context.getResources();
    this.maxImageWidthForMemoryCache = builder.maxImageWidthForMemoryCache;
    this.maxImageHeightForMemoryCache = builder.maxImageHeightForMemoryCache;
    this.maxImageWidthForDiskCache = builder.maxImageWidthForDiskCache;
    this.maxImageHeightForDiskCache = builder.maxImageHeightForDiskCache;
    this.processorForDiskCache = builder.processorForDiskCache;
    this.taskExecutor = builder.taskExecutor;
    this.taskExecutorForCachedImages = builder.taskExecutorForCachedImages;
    this.threadPoolSize = builder.threadPoolSize;
    this.threadPriority = builder.threadPriority;
    this.tasksProcessingType = builder.tasksProcessingType;
    this.diskCache = builder.diskCache;
    this.memoryCache = builder.memoryCache;
    this.defaultDisplayImageOptions = builder.defaultDisplayImageOptions;
    this.downloader = builder.downloader;
    this.decoder = builder.decoder;
    this.customExecutor = builder.customExecutor;
    this.customExecutorForCachedImages = builder.customExecutorForCachedImages;
    this.networkDeniedDownloader = new io.rong.imageloader.core.ImageLoaderConfiguration.NetworkDeniedImageDownloader(this.downloader);
    this.slowNetworkDownloader = new io.rong.imageloader.core.ImageLoaderConfiguration.SlowNetworkImageDownloader(this.downloader);
    L.writeDebugLogs(builder.writeLogs);
  }

  public static io.rong.imageloader.core.ImageLoaderConfiguration createDefault(Context context) {
    return (new io.rong.imageloader.core.ImageLoaderConfiguration.Builder(context)).build();
  }

  ImageSize getMaxImageSize() {
    DisplayMetrics displayMetrics = this.resources.getDisplayMetrics();
    int width = this.maxImageWidthForMemoryCache;
    if (width <= 0) {
      width = displayMetrics.widthPixels;
    }

    int height = this.maxImageHeightForMemoryCache;
    if (height <= 0) {
      height = displayMetrics.heightPixels;
    }

    return new ImageSize(width, height);
  }

  private static class SlowNetworkImageDownloader implements ImageDownloader {
    private final ImageDownloader wrappedDownloader;

    public SlowNetworkImageDownloader(ImageDownloader wrappedDownloader) {
      this.wrappedDownloader = wrappedDownloader;
    }

    public InputStream getStream(String imageUri, Object extra) throws IOException {
      InputStream imageStream = this.wrappedDownloader.getStream(imageUri, extra);
      switch(Scheme.ofUri(imageUri)) {
        case HTTP:
        case HTTPS:
          return new FlushedInputStream(imageStream);
        default:
          return imageStream;
      }
    }
  }

  private static class NetworkDeniedImageDownloader implements ImageDownloader {
    private final ImageDownloader wrappedDownloader;

    public NetworkDeniedImageDownloader(ImageDownloader wrappedDownloader) {
      this.wrappedDownloader = wrappedDownloader;
    }

    public InputStream getStream(String imageUri, Object extra) throws IOException {
      switch(Scheme.ofUri(imageUri)) {
        case HTTP:
        case HTTPS:
          throw new IllegalStateException();
        default:
          return this.wrappedDownloader.getStream(imageUri, extra);
      }
    }
  }

  public static class Builder {
    private static final String WARNING_OVERLAP_DISK_CACHE_PARAMS = "diskCache(), diskCacheSize() and diskCacheFileCount calls overlap each other";
    private static final String WARNING_OVERLAP_DISK_CACHE_NAME_GENERATOR = "diskCache() and diskCacheFileNameGenerator() calls overlap each other";
    private static final String WARNING_OVERLAP_MEMORY_CACHE = "memoryCache() and memoryCacheSize() calls overlap each other";
    private static final String WARNING_OVERLAP_EXECUTOR = "threadPoolSize(), threadPriority() and tasksProcessingOrder() calls can overlap taskExecutor() and taskExecutorForCachedImages() calls.";
    public static final int DEFAULT_THREAD_POOL_SIZE = 3;
    public static final int DEFAULT_THREAD_PRIORITY = 3;
    public static final QueueProcessingType DEFAULT_TASK_PROCESSING_TYPE;
    private Context context;
    private int maxImageWidthForMemoryCache = 0;
    private int maxImageHeightForMemoryCache = 0;
    private int maxImageWidthForDiskCache = 0;
    private int maxImageHeightForDiskCache = 0;
    private BitmapProcessor processorForDiskCache = null;
    private Executor taskExecutor = null;
    private Executor taskExecutorForCachedImages = null;
    private boolean customExecutor = false;
    private boolean customExecutorForCachedImages = false;
    private int threadPoolSize = 3;
    private int threadPriority = 3;
    private boolean denyCacheImageMultipleSizesInMemory = false;
    private QueueProcessingType tasksProcessingType;
    private int memoryCacheSize;
    private long diskCacheSize;
    private int diskCacheFileCount;
    private MemoryCache memoryCache;
    private DiskCache diskCache;
    private FileNameGenerator diskCacheFileNameGenerator;
    private ImageDownloader downloader;
    private ImageDecoder decoder;
    private DisplayImageOptions defaultDisplayImageOptions;
    private boolean writeLogs;

    public Builder(Context context) {
      this.tasksProcessingType = DEFAULT_TASK_PROCESSING_TYPE;
      this.memoryCacheSize = 0;
      this.diskCacheSize = 0L;
      this.diskCacheFileCount = 0;
      this.memoryCache = null;
      this.diskCache = null;
      this.diskCacheFileNameGenerator = null;
      this.downloader = null;
      this.defaultDisplayImageOptions = null;
      this.writeLogs = false;
      this.context = context.getApplicationContext();
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder memoryCacheExtraOptions(int maxImageWidthForMemoryCache, int maxImageHeightForMemoryCache) {
      this.maxImageWidthForMemoryCache = maxImageWidthForMemoryCache;
      this.maxImageHeightForMemoryCache = maxImageHeightForMemoryCache;
      return this;
    }

    /** @deprecated */
    @Deprecated
    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder discCacheExtraOptions(int maxImageWidthForDiskCache, int maxImageHeightForDiskCache, BitmapProcessor processorForDiskCache) {
      return this.diskCacheExtraOptions(maxImageWidthForDiskCache, maxImageHeightForDiskCache, processorForDiskCache);
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder diskCacheExtraOptions(int maxImageWidthForDiskCache, int maxImageHeightForDiskCache, BitmapProcessor processorForDiskCache) {
      this.maxImageWidthForDiskCache = maxImageWidthForDiskCache;
      this.maxImageHeightForDiskCache = maxImageHeightForDiskCache;
      this.processorForDiskCache = processorForDiskCache;
      return this;
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder taskExecutor(Executor executor) {
      if (this.threadPoolSize != 3 || this.threadPriority != 3 || this.tasksProcessingType != DEFAULT_TASK_PROCESSING_TYPE) {
        L.w("threadPoolSize(), threadPriority() and tasksProcessingOrder() calls can overlap taskExecutor() and taskExecutorForCachedImages() calls.", new Object[0]);
      }

      this.taskExecutor = executor;
      return this;
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder taskExecutorForCachedImages(Executor executorForCachedImages) {
      if (this.threadPoolSize != 3 || this.threadPriority != 3 || this.tasksProcessingType != DEFAULT_TASK_PROCESSING_TYPE) {
        L.w("threadPoolSize(), threadPriority() and tasksProcessingOrder() calls can overlap taskExecutor() and taskExecutorForCachedImages() calls.", new Object[0]);
      }

      this.taskExecutorForCachedImages = executorForCachedImages;
      return this;
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder threadPoolSize(int threadPoolSize) {
      if (this.taskExecutor != null || this.taskExecutorForCachedImages != null) {
        L.w("threadPoolSize(), threadPriority() and tasksProcessingOrder() calls can overlap taskExecutor() and taskExecutorForCachedImages() calls.", new Object[0]);
      }

      this.threadPoolSize = threadPoolSize;
      return this;
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder threadPriority(int threadPriority) {
      if (this.taskExecutor != null || this.taskExecutorForCachedImages != null) {
        L.w("threadPoolSize(), threadPriority() and tasksProcessingOrder() calls can overlap taskExecutor() and taskExecutorForCachedImages() calls.", new Object[0]);
      }

      if (threadPriority < 1) {
        this.threadPriority = 1;
      } else if (threadPriority > 10) {
        this.threadPriority = 10;
      } else {
        this.threadPriority = threadPriority;
      }

      return this;
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder denyCacheImageMultipleSizesInMemory() {
      this.denyCacheImageMultipleSizesInMemory = true;
      return this;
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder tasksProcessingOrder(QueueProcessingType tasksProcessingType) {
      if (this.taskExecutor != null || this.taskExecutorForCachedImages != null) {
        L.w("threadPoolSize(), threadPriority() and tasksProcessingOrder() calls can overlap taskExecutor() and taskExecutorForCachedImages() calls.", new Object[0]);
      }

      this.tasksProcessingType = tasksProcessingType;
      return this;
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder memoryCacheSize(int memoryCacheSize) {
      if (memoryCacheSize <= 0) {
        throw new IllegalArgumentException("memoryCacheSize must be a positive number");
      } else {
        if (this.memoryCache != null) {
          L.w("memoryCache() and memoryCacheSize() calls overlap each other", new Object[0]);
        }

        this.memoryCacheSize = memoryCacheSize;
        return this;
      }
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder memoryCacheSizePercentage(int availableMemoryPercent) {
      if (availableMemoryPercent > 0 && availableMemoryPercent < 100) {
        if (this.memoryCache != null) {
          L.w("memoryCache() and memoryCacheSize() calls overlap each other", new Object[0]);
        }

        long availableMemory = Runtime.getRuntime().maxMemory();
        this.memoryCacheSize = (int)((float)availableMemory * ((float)availableMemoryPercent / 100.0F));
        return this;
      } else {
        throw new IllegalArgumentException("availableMemoryPercent must be in range (0 < % < 100)");
      }
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder memoryCache(MemoryCache memoryCache) {
      if (this.memoryCacheSize != 0) {
        L.w("memoryCache() and memoryCacheSize() calls overlap each other", new Object[0]);
      }

      this.memoryCache = memoryCache;
      return this;
    }

    /** @deprecated */
    @Deprecated
    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder discCacheSize(int maxCacheSize) {
      return this.diskCacheSize(maxCacheSize);
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder diskCacheSize(int maxCacheSize) {
      if (maxCacheSize <= 0) {
        throw new IllegalArgumentException("maxCacheSize must be a positive number");
      } else {
        if (this.diskCache != null) {
          L.w("diskCache(), diskCacheSize() and diskCacheFileCount calls overlap each other", new Object[0]);
        }

        this.diskCacheSize = (long)maxCacheSize;
        return this;
      }
    }

    /** @deprecated */
    @Deprecated
    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder discCacheFileCount(int maxFileCount) {
      return this.diskCacheFileCount(maxFileCount);
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder diskCacheFileCount(int maxFileCount) {
      if (maxFileCount <= 0) {
        throw new IllegalArgumentException("maxFileCount must be a positive number");
      } else {
        if (this.diskCache != null) {
          L.w("diskCache(), diskCacheSize() and diskCacheFileCount calls overlap each other", new Object[0]);
        }

        this.diskCacheFileCount = maxFileCount;
        return this;
      }
    }

    /** @deprecated */
    @Deprecated
    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder discCacheFileNameGenerator(FileNameGenerator fileNameGenerator) {
      return this.diskCacheFileNameGenerator(fileNameGenerator);
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder diskCacheFileNameGenerator(FileNameGenerator fileNameGenerator) {
      if (this.diskCache != null) {
        L.w("diskCache() and diskCacheFileNameGenerator() calls overlap each other", new Object[0]);
      }

      this.diskCacheFileNameGenerator = fileNameGenerator;
      return this;
    }

    /** @deprecated */
    @Deprecated
    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder discCache(DiskCache diskCache) {
      return this.diskCache(diskCache);
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder diskCache(DiskCache diskCache) {
      if (this.diskCacheSize > 0L || this.diskCacheFileCount > 0) {
        L.w("diskCache(), diskCacheSize() and diskCacheFileCount calls overlap each other", new Object[0]);
      }

      if (this.diskCacheFileNameGenerator != null) {
        L.w("diskCache() and diskCacheFileNameGenerator() calls overlap each other", new Object[0]);
      }

      this.diskCache = diskCache;
      return this;
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder imageDownloader(ImageDownloader imageDownloader) {
      this.downloader = imageDownloader;
      return this;
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder imageDecoder(ImageDecoder imageDecoder) {
      this.decoder = imageDecoder;
      return this;
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder defaultDisplayImageOptions(DisplayImageOptions defaultDisplayImageOptions) {
      this.defaultDisplayImageOptions = defaultDisplayImageOptions;
      return this;
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration.Builder writeDebugLogs() {
      this.writeLogs = true;
      return this;
    }

    public io.rong.imageloader.core.ImageLoaderConfiguration build() {
      this.initEmptyFieldsWithDefaultValues();
      return new io.rong.imageloader.core.ImageLoaderConfiguration(this);
    }

    private void initEmptyFieldsWithDefaultValues() {
      if (this.taskExecutor == null) {
        this.taskExecutor = DefaultConfigurationFactory.createExecutor(this.threadPoolSize, this.threadPriority, this.tasksProcessingType);
      } else {
        this.customExecutor = true;
      }

      if (this.taskExecutorForCachedImages == null) {
        this.taskExecutorForCachedImages = DefaultConfigurationFactory.createExecutor(this.threadPoolSize, this.threadPriority, this.tasksProcessingType);
      } else {
        this.customExecutorForCachedImages = true;
      }

      if (this.diskCache == null) {
        if (this.diskCacheFileNameGenerator == null) {
          this.diskCacheFileNameGenerator = DefaultConfigurationFactory.createFileNameGenerator();
        }

        this.diskCache = DefaultConfigurationFactory.createDiskCache(this.context, this.diskCacheFileNameGenerator, this.diskCacheSize, this.diskCacheFileCount);
      }

      if (this.memoryCache == null) {
        this.memoryCache = DefaultConfigurationFactory.createMemoryCache(this.context, this.memoryCacheSize);
      }

      if (this.denyCacheImageMultipleSizesInMemory) {
        this.memoryCache = new FuzzyKeyMemoryCache(this.memoryCache, MemoryCacheUtils.createFuzzyKeyComparator());
      }

      if (this.downloader == null) {
        this.downloader = DefaultConfigurationFactory.createImageDownloader(this.context);
      }

      if (this.decoder == null) {
        this.decoder = DefaultConfigurationFactory.createImageDecoder(this.writeLogs);
      }

      if (this.defaultDisplayImageOptions == null) {
        this.defaultDisplayImageOptions = DisplayImageOptions.createSimple();
      }

    }

    static {
      DEFAULT_TASK_PROCESSING_TYPE = QueueProcessingType.FIFO;
    }
  }
}
