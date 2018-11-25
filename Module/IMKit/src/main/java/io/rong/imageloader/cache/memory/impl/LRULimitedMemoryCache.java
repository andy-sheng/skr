//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.cache.memory.impl;

import android.graphics.Bitmap;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.rong.imageloader.cache.memory.LimitedMemoryCache;

public class LRULimitedMemoryCache extends LimitedMemoryCache {
  private static final int INITIAL_CAPACITY = 10;
  private static final float LOAD_FACTOR = 1.1F;
  private final Map<String, Bitmap> lruCache = Collections.synchronizedMap(new LinkedHashMap(10, 1.1F, true));

  public LRULimitedMemoryCache(int maxSize) {
    super(maxSize);
  }

  public boolean put(String key, Bitmap value) {
    if (super.put(key, value)) {
      this.lruCache.put(key, value);
      return true;
    } else {
      return false;
    }
  }

  public Bitmap get(String key) {
    this.lruCache.get(key);
    return super.get(key);
  }

  public Bitmap remove(String key) {
    this.lruCache.remove(key);
    return super.remove(key);
  }

  public void clear() {
    this.lruCache.clear();
    super.clear();
  }

  protected int getSize(Bitmap value) {
    return value.getRowBytes() * value.getHeight();
  }

  protected Bitmap removeNext() {
    Bitmap mostLongUsedValue = null;
    Map var2 = this.lruCache;
    synchronized(this.lruCache) {
      Iterator<Entry<String, Bitmap>> it = this.lruCache.entrySet().iterator();
      if (it.hasNext()) {
        Entry<String, Bitmap> entry = (Entry)it.next();
        mostLongUsedValue = (Bitmap)entry.getValue();
        it.remove();
      }

      return mostLongUsedValue;
    }
  }

  protected Reference<Bitmap> createReference(Bitmap value) {
    return new WeakReference(value);
  }
}
