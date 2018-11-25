//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core;

import java.util.concurrent.locks.ReentrantLock;

import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.assist.ImageSize;
import io.rong.imageloader.core.imageaware.ImageAware;
import io.rong.imageloader.core.listener.ImageLoadingListener;
import io.rong.imageloader.core.listener.ImageLoadingProgressListener;

final class ImageLoadingInfo {
  final String uri;
  final String memoryCacheKey;
  final ImageAware imageAware;
  final ImageSize targetSize;
  final DisplayImageOptions options;
  final ImageLoadingListener listener;
  final ImageLoadingProgressListener progressListener;
  final ReentrantLock loadFromUriLock;

  public ImageLoadingInfo(String uri, ImageAware imageAware, ImageSize targetSize, String memoryCacheKey, DisplayImageOptions options, ImageLoadingListener listener, ImageLoadingProgressListener progressListener, ReentrantLock loadFromUriLock) {
    this.uri = uri;
    this.imageAware = imageAware;
    this.targetSize = targetSize;
    this.options = options;
    this.listener = listener;
    this.progressListener = progressListener;
    this.loadFromUriLock = loadFromUriLock;
    this.memoryCacheKey = memoryCacheKey;
  }
}
