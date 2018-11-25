//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.cache.disc.impl;

import java.io.File;

import io.rong.imageloader.cache.disc.impl.BaseDiskCache;
import io.rong.imageloader.cache.disc.naming.FileNameGenerator;

public class UnlimitedDiskCache extends BaseDiskCache {
  public UnlimitedDiskCache(File cacheDir) {
    super(cacheDir);
  }

  public UnlimitedDiskCache(File cacheDir, File reserveCacheDir) {
    super(cacheDir, reserveCacheDir);
  }

  public UnlimitedDiskCache(File cacheDir, File reserveCacheDir, FileNameGenerator fileNameGenerator) {
    super(cacheDir, reserveCacheDir, fileNameGenerator);
  }
}
