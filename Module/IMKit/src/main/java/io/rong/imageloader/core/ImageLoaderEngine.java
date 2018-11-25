//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import io.rong.imageloader.core.DefaultConfigurationFactory;
import io.rong.imageloader.core.ImageLoaderConfiguration;
import io.rong.imageloader.core.imageaware.ImageAware;

class ImageLoaderEngine {
  final ImageLoaderConfiguration configuration;
  private Executor taskExecutor;
  private Executor taskExecutorForCachedImages;
  private Executor taskDistributor;
  private final Map<Integer, String> cacheKeysForImageAwares = Collections.synchronizedMap(new HashMap());
  private final Map<String, ReentrantLock> uriLocks = new WeakHashMap();
  private final AtomicBoolean paused = new AtomicBoolean(false);
  private final AtomicBoolean networkDenied = new AtomicBoolean(false);
  private final AtomicBoolean slowNetwork = new AtomicBoolean(false);
  private final Object pauseLock = new Object();

  ImageLoaderEngine(ImageLoaderConfiguration configuration) {
    this.configuration = configuration;
    this.taskExecutor = configuration.taskExecutor;
    this.taskExecutorForCachedImages = configuration.taskExecutorForCachedImages;
    this.taskDistributor = DefaultConfigurationFactory.createTaskDistributor();
  }

  void submit(final LoadAndDisplayImageTask task) {
    this.taskDistributor.execute(new Runnable() {
      public void run() {
        File image = io.rong.imageloader.core.ImageLoaderEngine.this.configuration.diskCache.get(task.getLoadingUri());
        boolean isImageCachedOnDisk = image != null && image.exists();
        io.rong.imageloader.core.ImageLoaderEngine.this.initExecutorsIfNeed();
        if (isImageCachedOnDisk) {
          io.rong.imageloader.core.ImageLoaderEngine.this.taskExecutorForCachedImages.execute(task);
        } else {
          io.rong.imageloader.core.ImageLoaderEngine.this.taskExecutor.execute(task);
        }

      }
    });
  }

  void submit(ProcessAndDisplayImageTask task) {
    this.initExecutorsIfNeed();
    this.taskExecutorForCachedImages.execute(task);
  }

  private void initExecutorsIfNeed() {
    if (!this.configuration.customExecutor && ((ExecutorService)this.taskExecutor).isShutdown()) {
      this.taskExecutor = this.createTaskExecutor();
    }

    if (!this.configuration.customExecutorForCachedImages && ((ExecutorService)this.taskExecutorForCachedImages).isShutdown()) {
      this.taskExecutorForCachedImages = this.createTaskExecutor();
    }

  }

  private Executor createTaskExecutor() {
    return DefaultConfigurationFactory.createExecutor(this.configuration.threadPoolSize, this.configuration.threadPriority, this.configuration.tasksProcessingType);
  }

  String getLoadingUriForView(ImageAware imageAware) {
    return (String)this.cacheKeysForImageAwares.get(imageAware.getId());
  }

  void prepareDisplayTaskFor(ImageAware imageAware, String memoryCacheKey) {
    this.cacheKeysForImageAwares.put(imageAware.getId(), memoryCacheKey);
  }

  void cancelDisplayTaskFor(ImageAware imageAware) {
    this.cacheKeysForImageAwares.remove(imageAware.getId());
  }

  void denyNetworkDownloads(boolean denyNetworkDownloads) {
    this.networkDenied.set(denyNetworkDownloads);
  }

  void handleSlowNetwork(boolean handleSlowNetwork) {
    this.slowNetwork.set(handleSlowNetwork);
  }

  void pause() {
    this.paused.set(true);
  }

  void resume() {
    this.paused.set(false);
    Object var1 = this.pauseLock;
    synchronized(this.pauseLock) {
      this.pauseLock.notifyAll();
    }
  }

  void stop() {
    if (!this.configuration.customExecutor) {
      ((ExecutorService)this.taskExecutor).shutdownNow();
    }

    if (!this.configuration.customExecutorForCachedImages) {
      ((ExecutorService)this.taskExecutorForCachedImages).shutdownNow();
    }

    this.cacheKeysForImageAwares.clear();
    this.uriLocks.clear();
  }

  void fireCallback(Runnable r) {
    this.taskDistributor.execute(r);
  }

  ReentrantLock getLockForUri(String uri) {
    ReentrantLock lock = (ReentrantLock)this.uriLocks.get(uri);
    if (lock == null) {
      lock = new ReentrantLock();
      this.uriLocks.put(uri, lock);
    }

    return lock;
  }

  AtomicBoolean getPause() {
    return this.paused;
  }

  Object getPauseLock() {
    return this.pauseLock;
  }

  boolean isNetworkDenied() {
    return this.networkDenied.get();
  }

  boolean isSlowNetwork() {
    return this.slowNetwork.get();
  }
}
