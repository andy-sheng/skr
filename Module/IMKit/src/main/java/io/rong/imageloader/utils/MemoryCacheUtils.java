//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.utils;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import io.rong.imageloader.cache.memory.MemoryCache;
import io.rong.imageloader.core.assist.ImageSize;

public final class MemoryCacheUtils {
  private static final String URI_AND_SIZE_SEPARATOR = "_";
  private static final String WIDTH_AND_HEIGHT_SEPARATOR = "x";

  private MemoryCacheUtils() {
  }

  public static String generateKey(String imageUri, ImageSize targetSize) {
    return imageUri + "_" + targetSize.getWidth() + "x" + targetSize.getHeight();
  }

  public static Comparator<String> createFuzzyKeyComparator() {
    return new Comparator<String>() {
      public int compare(String key1, String key2) {
        String imageUri1 = key1.substring(0, key1.lastIndexOf("_"));
        String imageUri2 = key2.substring(0, key2.lastIndexOf("_"));
        return imageUri1.compareTo(imageUri2);
      }
    };
  }

  public static List<Bitmap> findCachedBitmapsForImageUri(String imageUri, MemoryCache memoryCache) {
    List<Bitmap> values = new ArrayList();
    Iterator var3 = memoryCache.keys().iterator();

    while(var3.hasNext()) {
      String key = (String)var3.next();
      if (key.startsWith(imageUri)) {
        values.add(memoryCache.get(key));
      }
    }

    return values;
  }

  public static List<String> findCacheKeysForImageUri(String imageUri, MemoryCache memoryCache) {
    List<String> values = new ArrayList();
    Iterator var3 = memoryCache.keys().iterator();

    while(var3.hasNext()) {
      String key = (String)var3.next();
      if (key.startsWith(imageUri)) {
        values.add(key);
      }
    }

    return values;
  }

  public static void removeFromCache(String imageUri, MemoryCache memoryCache) {
    List<String> keysToRemove = new ArrayList();
    Iterator var3 = memoryCache.keys().iterator();

    String keyToRemove;
    while(var3.hasNext()) {
      keyToRemove = (String)var3.next();
      if (keyToRemove.startsWith(imageUri)) {
        keysToRemove.add(keyToRemove);
      }
    }

    var3 = keysToRemove.iterator();

    while(var3.hasNext()) {
      keyToRemove = (String)var3.next();
      memoryCache.remove(keyToRemove);
    }

  }
}
