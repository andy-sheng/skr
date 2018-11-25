//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.cache.memory.impl;

import android.graphics.Bitmap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.rong.imageloader.cache.memory.MemoryCache;

public class LimitedAgeMemoryCache implements MemoryCache {
  private final MemoryCache cache;
  private final long maxAge;
  private final Map<String, Long> loadingDates = Collections.synchronizedMap(new HashMap());

  public LimitedAgeMemoryCache(MemoryCache cache, long maxAge) {
    this.cache = cache;
    this.maxAge = maxAge * 1000L;
  }

  public boolean put(String key, Bitmap value) {
    boolean putSuccesfully = this.cache.put(key, value);
    if (putSuccesfully) {
      this.loadingDates.put(key, System.currentTimeMillis());
    }

    return putSuccesfully;
  }

  public Bitmap get(String key) {
    Long loadingDate = (Long)this.loadingDates.get(key);
    if (loadingDate != null && System.currentTimeMillis() - loadingDate > this.maxAge) {
      this.cache.remove(key);
      this.loadingDates.remove(key);
    }

    return this.cache.get(key);
  }

  public Bitmap remove(String key) {
    this.loadingDates.remove(key);
    return this.cache.remove(key);
  }

  public Collection<String> keys() {
    return this.cache.keys();
  }

  public void clear() {
    this.cache.clear();
    this.loadingDates.clear();
  }
}
