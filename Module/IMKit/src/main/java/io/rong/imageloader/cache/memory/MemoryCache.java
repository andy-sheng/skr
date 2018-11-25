//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.cache.memory;

import android.graphics.Bitmap;

import java.util.Collection;

public interface MemoryCache {
  boolean put(String var1, Bitmap var2);

  Bitmap get(String var1);

  Bitmap remove(String var1);

  Collection<String> keys();

  void clear();
}
