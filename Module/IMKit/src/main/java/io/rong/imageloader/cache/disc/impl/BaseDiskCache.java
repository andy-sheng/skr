//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.cache.disc.impl;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.rong.imageloader.cache.disc.DiskCache;
import io.rong.imageloader.cache.disc.naming.FileNameGenerator;
import io.rong.imageloader.core.DefaultConfigurationFactory;
import io.rong.imageloader.utils.IoUtils;
import io.rong.imageloader.utils.IoUtils.CopyListener;

public abstract class BaseDiskCache implements DiskCache {
  public static final int DEFAULT_BUFFER_SIZE = 32768;
  public static final CompressFormat DEFAULT_COMPRESS_FORMAT;
  public static final int DEFAULT_COMPRESS_QUALITY = 100;
  private static final String ERROR_ARG_NULL = " argument must be not null";
  private static final String TEMP_IMAGE_POSTFIX = ".tmp";
  protected final File cacheDir;
  protected final File reserveCacheDir;
  protected final FileNameGenerator fileNameGenerator;
  protected int bufferSize;
  protected CompressFormat compressFormat;
  protected int compressQuality;

  public BaseDiskCache(File cacheDir) {
    this(cacheDir, (File)null);
  }

  public BaseDiskCache(File cacheDir, File reserveCacheDir) {
    this(cacheDir, reserveCacheDir, DefaultConfigurationFactory.createFileNameGenerator());
  }

  public BaseDiskCache(File cacheDir, File reserveCacheDir, FileNameGenerator fileNameGenerator) {
    this.bufferSize = 32768;
    this.compressFormat = DEFAULT_COMPRESS_FORMAT;
    this.compressQuality = 100;
    if (cacheDir == null) {
      throw new IllegalArgumentException("cacheDir argument must be not null");
    } else if (fileNameGenerator == null) {
      throw new IllegalArgumentException("fileNameGenerator argument must be not null");
    } else {
      this.cacheDir = cacheDir;
      this.reserveCacheDir = reserveCacheDir;
      this.fileNameGenerator = fileNameGenerator;
    }
  }

  public File getDirectory() {
    return this.cacheDir;
  }

  public File get(String imageUri) {
    return this.getFile(imageUri);
  }

  public boolean save(String imageUri, InputStream imageStream, CopyListener listener) throws IOException {
    File imageFile = this.getFile(imageUri);
    File tmpFile = new File(imageFile.getAbsolutePath() + ".tmp");
    boolean loaded = false;

    try {
      BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile), this.bufferSize);

      try {
        loaded = IoUtils.copyStream(imageStream, os, listener, this.bufferSize);
      } finally {
        IoUtils.closeSilently(os);
      }
    } finally {
      if (loaded && !tmpFile.renameTo(imageFile)) {
        loaded = false;
      }

      if (!loaded) {
        tmpFile.delete();
      }

    }

    return loaded;
  }

  public boolean save(String imageUri, Bitmap bitmap) throws IOException {
    File imageFile = this.getFile(imageUri);
    File tmpFile = new File(imageFile.getAbsolutePath() + ".tmp");
    OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile), this.bufferSize);
    boolean savedSuccessfully = false;

    try {
      savedSuccessfully = bitmap.compress(this.compressFormat, this.compressQuality, os);
    } finally {
      IoUtils.closeSilently(os);
      if (savedSuccessfully && !tmpFile.renameTo(imageFile)) {
        savedSuccessfully = false;
      }

      if (!savedSuccessfully) {
        tmpFile.delete();
      }

    }

    bitmap.recycle();
    return savedSuccessfully;
  }

  public boolean remove(String imageUri) {
    return this.getFile(imageUri).delete();
  }

  public void close() {
  }

  public void clear() {
    File[] files = this.cacheDir.listFiles();
    if (files != null) {
      File[] var2 = files;
      int var3 = files.length;

      for(int var4 = 0; var4 < var3; ++var4) {
        File f = var2[var4];
        f.delete();
      }
    }

  }

  protected File getFile(String imageUri) {
    String fileName = this.fileNameGenerator.generate(imageUri);
    File dir = this.cacheDir;
    if (!this.cacheDir.exists() && !this.cacheDir.mkdirs() && this.reserveCacheDir != null && (this.reserveCacheDir.exists() || this.reserveCacheDir.mkdirs())) {
      dir = this.reserveCacheDir;
    }

    return new File(dir, fileName);
  }

  public void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
  }

  public void setCompressFormat(CompressFormat compressFormat) {
    this.compressFormat = compressFormat;
  }

  public void setCompressQuality(int compressQuality) {
    this.compressQuality = compressQuality;
  }

  static {
    DEFAULT_COMPRESS_FORMAT = CompressFormat.PNG;
  }
}
