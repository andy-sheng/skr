//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.cache.memory;

import android.graphics.Bitmap;

import java.lang.ref.Reference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import io.rong.imageloader.cache.memory.MemoryCache;

public abstract class BaseMemoryCache implements MemoryCache {
  private final Map<String, Reference<Bitmap>> softMap = Collections.synchronizedMap(new HashMap());

  public BaseMemoryCache() {
  }

  public Bitmap get(String key) {
    Bitmap result = null;
    Reference<Bitmap> reference = (Reference)this.softMap.get(key);
    if (reference != null) {
      result = (Bitmap)reference.get();
    }

    return result;
  }

  public boolean put(String key, Bitmap value) {
    this.softMap.put(key, this.createReference(value));
    return true;
  }

  public Bitmap remove(String key) {
    Reference<Bitmap> bmpRef = (Reference)this.softMap.remove(key);
    return bmpRef == null ? null : (Bitmap)bmpRef.get();
  }

  public Collection<String> keys() {
    Map var1 = this.softMap;
    synchronized(this.softMap) {
      return new HashSet(this.softMap.keySet());
    }
  }

  public void clear() {
    this.softMap.clear();
  }

  protected abstract Reference<Bitmap> createReference(Bitmap var1);
}
