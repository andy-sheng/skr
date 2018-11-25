//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.cache.memory;

import android.graphics.Bitmap;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.rong.imageloader.cache.memory.BaseMemoryCache;
import io.rong.imageloader.utils.L;

public abstract class LimitedMemoryCache extends BaseMemoryCache {
  private static final int MAX_NORMAL_CACHE_SIZE_IN_MB = 16;
  private static final int MAX_NORMAL_CACHE_SIZE = 16777216;
  private final int sizeLimit;
  private final AtomicInteger cacheSize;
  private final List<Bitmap> hardCache = Collections.synchronizedList(new LinkedList());

  public LimitedMemoryCache(int sizeLimit) {
    this.sizeLimit = sizeLimit;
    this.cacheSize = new AtomicInteger();
    if (sizeLimit > 16777216) {
      L.w("You set too large memory cache size (more than %1$d Mb)", new Object[]{16});
    }

  }

  public boolean put(String key, Bitmap value) {
    boolean putSuccessfully = false;
    int valueSize = this.getSize(value);
    int sizeLimit = this.getSizeLimit();
    int curCacheSize = this.cacheSize.get();
    if (valueSize < sizeLimit) {
      while(true) {
        if (curCacheSize + valueSize <= sizeLimit) {
          this.hardCache.add(value);
          this.cacheSize.addAndGet(valueSize);
          putSuccessfully = true;
          break;
        }

        Bitmap removedValue = this.removeNext();
        if (this.hardCache.remove(removedValue)) {
          curCacheSize = this.cacheSize.addAndGet(-this.getSize(removedValue));
        }
      }
    }

    super.put(key, value);
    return putSuccessfully;
  }

  public Bitmap remove(String key) {
    Bitmap value = super.get(key);
    if (value != null && this.hardCache.remove(value)) {
      this.cacheSize.addAndGet(-this.getSize(value));
    }

    return super.remove(key);
  }

  public void clear() {
    this.hardCache.clear();
    this.cacheSize.set(0);
    super.clear();
  }

  protected int getSizeLimit() {
    return this.sizeLimit;
  }

  protected abstract int getSize(Bitmap var1);

  protected abstract Bitmap removeNext();
}
