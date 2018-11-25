//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.cache.disc;

import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import io.rong.imageloader.utils.IoUtils.CopyListener;

public interface DiskCache {
  File getDirectory();

  File get(String var1);

  boolean save(String var1, InputStream var2, CopyListener var3) throws IOException;

  boolean save(String var1, Bitmap var2) throws IOException;

  boolean remove(String var1);

  void close();

  void clear();
}
