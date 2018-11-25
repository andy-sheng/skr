//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.utils;

import java.io.File;

import io.rong.imageloader.cache.disc.DiskCache;

public final class DiskCacheUtils {
  private DiskCacheUtils() {
  }

  public static File findInCache(String imageUri, DiskCache diskCache) {
    File image = diskCache.get(imageUri);
    return image != null && image.exists() ? image : null;
  }

  public static boolean removeFromCache(String imageUri, DiskCache diskCache) {
    File image = diskCache.get(imageUri);
    return image != null && image.exists() && image.delete();
  }
}
