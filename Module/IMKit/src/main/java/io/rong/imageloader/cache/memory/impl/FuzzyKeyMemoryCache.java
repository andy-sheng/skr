//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.cache.memory.impl;

import android.graphics.Bitmap;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import io.rong.imageloader.cache.memory.MemoryCache;

public class FuzzyKeyMemoryCache implements MemoryCache {
  private final MemoryCache cache;
  private final Comparator<String> keyComparator;

  public FuzzyKeyMemoryCache(MemoryCache cache, Comparator<String> keyComparator) {
    this.cache = cache;
    this.keyComparator = keyComparator;
  }

  public boolean put(String key, Bitmap value) {
    MemoryCache var3 = this.cache;
    synchronized(this.cache) {
      String keyToRemove = null;
      Iterator var5 = this.cache.keys().iterator();

      while(true) {
        if (var5.hasNext()) {
          String cacheKey = (String)var5.next();
          if (this.keyComparator.compare(key, cacheKey) != 0) {
            continue;
          }

          keyToRemove = cacheKey;
        }

        if (keyToRemove != null) {
          this.cache.remove(keyToRemove);
        }
        break;
      }
    }

    return this.cache.put(key, value);
  }

  public Bitmap get(String key) {
    return this.cache.get(key);
  }

  public Bitmap remove(String key) {
    return this.cache.remove(key);
  }

  public void clear() {
    this.cache.clear();
  }

  public Collection<String> keys() {
    return this.cache.keys();
  }
}
