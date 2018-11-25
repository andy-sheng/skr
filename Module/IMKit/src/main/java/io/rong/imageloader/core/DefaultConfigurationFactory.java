//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build.VERSION;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.rong.imageloader.cache.disc.DiskCache;
import io.rong.imageloader.cache.disc.impl.UnlimitedDiskCache;
import io.rong.imageloader.cache.disc.impl.ext.LruDiskCache;
import io.rong.imageloader.cache.disc.naming.FileNameGenerator;
import io.rong.imageloader.cache.disc.naming.HashCodeFileNameGenerator;
import io.rong.imageloader.cache.memory.MemoryCache;
import io.rong.imageloader.cache.memory.impl.LruMemoryCache;
import io.rong.imageloader.core.assist.QueueProcessingType;
import io.rong.imageloader.core.assist.deque.LIFOLinkedBlockingDeque;
import io.rong.imageloader.core.decode.BaseImageDecoder;
import io.rong.imageloader.core.decode.ImageDecoder;
import io.rong.imageloader.core.display.BitmapDisplayer;
import io.rong.imageloader.core.display.SimpleBitmapDisplayer;
import io.rong.imageloader.core.download.BaseImageDownloader;
import io.rong.imageloader.core.download.ImageDownloader;
import io.rong.imageloader.utils.L;
import io.rong.imageloader.utils.StorageUtils;

public class DefaultConfigurationFactory {
  public DefaultConfigurationFactory() {
  }

  public static Executor createExecutor(int threadPoolSize, int threadPriority, QueueProcessingType tasksProcessingType) {
    boolean lifo = tasksProcessingType == QueueProcessingType.LIFO;
    BlockingQueue<Runnable> taskQueue = (BlockingQueue)(lifo ? new LIFOLinkedBlockingDeque() : new LinkedBlockingQueue());
    return new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS, taskQueue, createThreadFactory(threadPriority, "uil-pool-"));
  }

  public static Executor createTaskDistributor() {
    return Executors.newCachedThreadPool(createThreadFactory(5, "uil-pool-d-"));
  }

  public static FileNameGenerator createFileNameGenerator() {
    return new HashCodeFileNameGenerator();
  }

  public static DiskCache createDiskCache(Context context, FileNameGenerator diskCacheFileNameGenerator, long diskCacheSize, int diskCacheFileCount) {
    File reserveCacheDir = createReserveDiskCacheDir(context);
    File individualCacheDir;
    if (diskCacheSize > 0L || diskCacheFileCount > 0) {
      individualCacheDir = StorageUtils.getIndividualCacheDirectory(context);

      try {
        return new LruDiskCache(individualCacheDir, reserveCacheDir, diskCacheFileNameGenerator, diskCacheSize, diskCacheFileCount);
      } catch (IOException var8) {
        L.e(var8);
      }
    }

    individualCacheDir = StorageUtils.getCacheDirectory(context);
    return new UnlimitedDiskCache(individualCacheDir, reserveCacheDir, diskCacheFileNameGenerator);
  }

  private static File createReserveDiskCacheDir(Context context) {
    File cacheDir = StorageUtils.getCacheDirectory(context, false);
    File individualDir = new File(cacheDir, "uil-images");
    if (individualDir.exists() || individualDir.mkdir()) {
      cacheDir = individualDir;
    }

    return cacheDir;
  }

  public static MemoryCache createMemoryCache(Context context, int memoryCacheSize) {
    if (memoryCacheSize == 0) {
      ActivityManager am = (ActivityManager)context.getSystemService("activity");
      int memoryClass = am.getMemoryClass();
      if (hasHoneycomb() && isLargeHeap(context)) {
        memoryClass = getLargeMemoryClass(am);
      }

      memoryCacheSize = 1048576 * memoryClass / 8;
    }

    return new LruMemoryCache(memoryCacheSize);
  }

  private static boolean hasHoneycomb() {
    return VERSION.SDK_INT >= 11;
  }

  @TargetApi(11)
  private static boolean isLargeHeap(Context context) {
    return (context.getApplicationInfo().flags & 1048576) != 0;
  }

  @TargetApi(11)
  private static int getLargeMemoryClass(ActivityManager am) {
    return am.getLargeMemoryClass();
  }

  public static ImageDownloader createImageDownloader(Context context) {
    return new BaseImageDownloader(context);
  }

  public static ImageDecoder createImageDecoder(boolean loggingEnabled) {
    return new BaseImageDecoder(loggingEnabled);
  }

  public static BitmapDisplayer createBitmapDisplayer() {
    return new SimpleBitmapDisplayer();
  }

  private static ThreadFactory createThreadFactory(int threadPriority, String threadNamePrefix) {
    return new io.rong.imageloader.core.DefaultConfigurationFactory.DefaultThreadFactory(threadPriority, threadNamePrefix);
  }

  private static class DefaultThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final int threadPriority;

    DefaultThreadFactory(int threadPriority, String threadNamePrefix) {
      this.threadPriority = threadPriority;
      this.group = Thread.currentThread().getThreadGroup();
      this.namePrefix = threadNamePrefix + poolNumber.getAndIncrement() + "-thread-";
    }

    public Thread newThread(Runnable r) {
      Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
      if (t.isDaemon()) {
        t.setDaemon(false);
      }

      t.setPriority(this.threadPriority);
      return t;
    }
  }
}
