//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.cache.memory.impl;

import android.graphics.Bitmap;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import io.rong.imageloader.cache.memory.MemoryCache;

public class LruMemoryCache implements MemoryCache {
  private final LinkedHashMap<String, Bitmap> map;
  private final int maxSize;
  private int size;

  public LruMemoryCache(int maxSize) {
    if (maxSize <= 0) {
      throw new IllegalArgumentException("maxSize <= 0");
    } else {
      this.maxSize = maxSize;
      this.map = new LinkedHashMap(0, 0.75F, true);
    }
  }

  public final Bitmap get(String key) {
    if (key == null) {
      throw new NullPointerException("key == null");
    } else {
      synchronized(this) {
        return (Bitmap)this.map.get(key);
      }
    }
  }

  public final boolean put(String key, Bitmap value) {
    if (key != null && value != null) {
      synchronized(this) {
        this.size += this.sizeOf(key, value);
        Bitmap previous = (Bitmap)this.map.put(key, value);
        if (previous != null) {
          this.size -= this.sizeOf(key, previous);
        }
      }

      this.trimToSize(this.maxSize);
      return true;
    } else {
      throw new NullPointerException("key == null || value == null");
    }
  }

  private void trimToSize(int maxSize) {
    while(true) {
      synchronized(this) {
        if (this.size < 0 || this.map.isEmpty() && this.size != 0) {
          throw new IllegalStateException(this.getClass().getName() + ".sizeOf() is reporting inconsistent results!");
        }

        if (this.size > maxSize && !this.map.isEmpty()) {
          Entry<String, Bitmap> toEvict = (Entry)this.map.entrySet().iterator().next();
          if (toEvict != null) {
            String key = (String)toEvict.getKey();
            Bitmap value = (Bitmap)toEvict.getValue();
            this.map.remove(key);
            this.size -= this.sizeOf(key, value);
            continue;
          }
        }

        return;
      }
    }
  }

  public final Bitmap remove(String key) {
    if (key == null) {
      throw new NullPointerException("key == null");
    } else {
      synchronized(this) {
        Bitmap previous = (Bitmap)this.map.remove(key);
        if (previous != null) {
          this.size -= this.sizeOf(key, previous);
        }

        return previous;
      }
    }
  }

  public Collection<String> keys() {
    synchronized(this) {
      return new HashSet(this.map.keySet());
    }
  }

  public void clear() {
    this.trimToSize(-1);
  }

  private int sizeOf(String key, Bitmap value) {
    return value.getRowBytes() * value.getHeight();
  }

  public final synchronized String toString() {
    return String.format("LruCache[maxSize=%d]", this.maxSize);
  }
}
