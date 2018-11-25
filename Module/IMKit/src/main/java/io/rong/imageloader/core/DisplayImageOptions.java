//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;

import io.rong.imageloader.core.DefaultConfigurationFactory;
import io.rong.imageloader.core.assist.ImageScaleType;
import io.rong.imageloader.core.display.BitmapDisplayer;
import io.rong.imageloader.core.process.BitmapProcessor;

public final class DisplayImageOptions {
  private final int imageResOnLoading;
  private final int imageResForEmptyUri;
  private final int imageResOnFail;
  private final Drawable imageOnLoading;
  private final Drawable imageForEmptyUri;
  private final Drawable imageOnFail;
  private final boolean resetViewBeforeLoading;
  private final boolean cacheInMemory;
  private final boolean cacheOnDisk;
  private final ImageScaleType imageScaleType;
  private final Options decodingOptions;
  private final int delayBeforeLoading;
  private final boolean considerExifParams;
  private final Object extraForDownloader;
  private final BitmapProcessor preProcessor;
  private final BitmapProcessor postProcessor;
  private final BitmapDisplayer displayer;
  private final Handler handler;
  private final boolean isSyncLoading;

  private DisplayImageOptions(io.rong.imageloader.core.DisplayImageOptions.Builder builder) {
    this.imageResOnLoading = builder.imageResOnLoading;
    this.imageResForEmptyUri = builder.imageResForEmptyUri;
    this.imageResOnFail = builder.imageResOnFail;
    this.imageOnLoading = builder.imageOnLoading;
    this.imageForEmptyUri = builder.imageForEmptyUri;
    this.imageOnFail = builder.imageOnFail;
    this.resetViewBeforeLoading = builder.resetViewBeforeLoading;
    this.cacheInMemory = builder.cacheInMemory;
    this.cacheOnDisk = builder.cacheOnDisk;
    this.imageScaleType = builder.imageScaleType;
    this.decodingOptions = builder.decodingOptions;
    this.delayBeforeLoading = builder.delayBeforeLoading;
    this.considerExifParams = builder.considerExifParams;
    this.extraForDownloader = builder.extraForDownloader;
    this.preProcessor = builder.preProcessor;
    this.postProcessor = builder.postProcessor;
    this.displayer = builder.displayer;
    this.handler = builder.handler;
    this.isSyncLoading = builder.isSyncLoading;
  }

  public boolean shouldShowImageOnLoading() {
    return this.imageOnLoading != null || this.imageResOnLoading != 0;
  }

  public boolean shouldShowImageForEmptyUri() {
    return this.imageForEmptyUri != null || this.imageResForEmptyUri != 0;
  }

  public Bitmap drawableToBitmap(Drawable drawable) {
    int width = drawable.getIntrinsicWidth();
    int height = drawable.getIntrinsicHeight();
    if (width > 0 && height > 0) {
      Config config = drawable.getOpacity() != -1 ? Config.ARGB_8888 : Config.RGB_565;
      Bitmap bitmap = Bitmap.createBitmap(width, height, config);
      Canvas canvas = new Canvas(bitmap);
      drawable.setBounds(0, 0, width, height);
      drawable.draw(canvas);
      return bitmap;
    } else {
      return null;
    }
  }

  public boolean shouldShowImageOnFail() {
    return this.imageOnFail != null || this.imageResOnFail != 0;
  }

  public boolean shouldPreProcess() {
    return this.preProcessor != null;
  }

  public boolean shouldPostProcess() {
    return this.postProcessor != null;
  }

  public boolean shouldDelayBeforeLoading() {
    return this.delayBeforeLoading > 0;
  }

  public Drawable getImageOnLoading(Resources res) {
    return this.imageResOnLoading != 0 ? res.getDrawable(this.imageResOnLoading) : this.imageOnLoading;
  }

  public Drawable getImageForEmptyUri(Resources res) {
    return this.imageResForEmptyUri != 0 ? res.getDrawable(this.imageResForEmptyUri) : this.imageForEmptyUri;
  }

  public Drawable getImageOnFail(Resources res) {
    return this.imageResOnFail != 0 ? res.getDrawable(this.imageResOnFail) : this.imageOnFail;
  }

  public boolean isResetViewBeforeLoading() {
    return this.resetViewBeforeLoading;
  }

  public boolean isCacheInMemory() {
    return this.cacheInMemory;
  }

  public boolean isCacheOnDisk() {
    return this.cacheOnDisk;
  }

  public ImageScaleType getImageScaleType() {
    return this.imageScaleType;
  }

  public Options getDecodingOptions() {
    return this.decodingOptions;
  }

  public int getDelayBeforeLoading() {
    return this.delayBeforeLoading;
  }

  public boolean isConsiderExifParams() {
    return this.considerExifParams;
  }

  public Object getExtraForDownloader() {
    return this.extraForDownloader;
  }

  public BitmapProcessor getPreProcessor() {
    return this.preProcessor;
  }

  public BitmapProcessor getPostProcessor() {
    return this.postProcessor;
  }

  public BitmapDisplayer getDisplayer() {
    return this.displayer;
  }

  public Handler getHandler() {
    return this.handler;
  }

  boolean isSyncLoading() {
    return this.isSyncLoading;
  }

  public static io.rong.imageloader.core.DisplayImageOptions createSimple() {
    return (new io.rong.imageloader.core.DisplayImageOptions.Builder()).build();
  }

  public static class Builder {
    private int imageResOnLoading = 0;
    private int imageResForEmptyUri = 0;
    private int imageResOnFail = 0;
    private Drawable imageOnLoading = null;
    private Drawable imageForEmptyUri = null;
    private Drawable imageOnFail = null;
    private boolean resetViewBeforeLoading = false;
    private boolean cacheInMemory = false;
    private boolean cacheOnDisk = false;
    private ImageScaleType imageScaleType;
    private Options decodingOptions;
    private int delayBeforeLoading;
    private boolean considerExifParams;
    private Object extraForDownloader;
    private BitmapProcessor preProcessor;
    private BitmapProcessor postProcessor;
    private BitmapDisplayer displayer;
    private Handler handler;
    private boolean isSyncLoading;

    public Builder() {
      this.imageScaleType = ImageScaleType.IN_SAMPLE_POWER_OF_2;
      this.decodingOptions = new Options();
      this.delayBeforeLoading = 0;
      this.considerExifParams = false;
      this.extraForDownloader = null;
      this.preProcessor = null;
      this.postProcessor = null;
      this.displayer = DefaultConfigurationFactory.createBitmapDisplayer();
      this.handler = null;
      this.isSyncLoading = false;
    }

    /** @deprecated */
    @Deprecated
    public io.rong.imageloader.core.DisplayImageOptions.Builder showStubImage(int imageRes) {
      this.imageResOnLoading = imageRes;
      return this;
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder showImageOnLoading(int imageRes) {
      this.imageResOnLoading = imageRes;
      return this;
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder showImageOnLoading(Drawable drawable) {
      this.imageOnLoading = drawable;
      return this;
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder showImageForEmptyUri(int imageRes) {
      this.imageResForEmptyUri = imageRes;
      return this;
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder showImageForEmptyUri(Drawable drawable) {
      this.imageForEmptyUri = drawable;
      return this;
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder showImageOnFail(int imageRes) {
      this.imageResOnFail = imageRes;
      return this;
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder showImageOnFail(Drawable drawable) {
      this.imageOnFail = drawable;
      return this;
    }

    /** @deprecated */
    public io.rong.imageloader.core.DisplayImageOptions.Builder resetViewBeforeLoading() {
      this.resetViewBeforeLoading = true;
      return this;
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder resetViewBeforeLoading(boolean resetViewBeforeLoading) {
      this.resetViewBeforeLoading = resetViewBeforeLoading;
      return this;
    }

    /** @deprecated */
    @Deprecated
    public io.rong.imageloader.core.DisplayImageOptions.Builder cacheInMemory() {
      this.cacheInMemory = true;
      return this;
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder cacheInMemory(boolean cacheInMemory) {
      this.cacheInMemory = cacheInMemory;
      return this;
    }

    /** @deprecated */
    @Deprecated
    public io.rong.imageloader.core.DisplayImageOptions.Builder cacheOnDisc() {
      return this.cacheOnDisk(true);
    }

    /** @deprecated */
    @Deprecated
    public io.rong.imageloader.core.DisplayImageOptions.Builder cacheOnDisc(boolean cacheOnDisk) {
      return this.cacheOnDisk(cacheOnDisk);
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder cacheOnDisk(boolean cacheOnDisk) {
      this.cacheOnDisk = cacheOnDisk;
      return this;
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder imageScaleType(ImageScaleType imageScaleType) {
      this.imageScaleType = imageScaleType;
      return this;
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder bitmapConfig(Config bitmapConfig) {
      if (bitmapConfig == null) {
        throw new IllegalArgumentException("bitmapConfig can't be null");
      } else {
        this.decodingOptions.inPreferredConfig = bitmapConfig;
        return this;
      }
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder decodingOptions(Options decodingOptions) {
      if (decodingOptions == null) {
        throw new IllegalArgumentException("decodingOptions can't be null");
      } else {
        this.decodingOptions = decodingOptions;
        return this;
      }
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder delayBeforeLoading(int delayInMillis) {
      this.delayBeforeLoading = delayInMillis;
      return this;
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder extraForDownloader(Object extra) {
      this.extraForDownloader = extra;
      return this;
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder considerExifParams(boolean considerExifParams) {
      this.considerExifParams = considerExifParams;
      return this;
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder preProcessor(BitmapProcessor preProcessor) {
      this.preProcessor = preProcessor;
      return this;
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder postProcessor(BitmapProcessor postProcessor) {
      this.postProcessor = postProcessor;
      return this;
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder displayer(BitmapDisplayer displayer) {
      if (displayer == null) {
        throw new IllegalArgumentException("displayer can't be null");
      } else {
        this.displayer = displayer;
        return this;
      }
    }

    io.rong.imageloader.core.DisplayImageOptions.Builder syncLoading(boolean isSyncLoading) {
      this.isSyncLoading = isSyncLoading;
      return this;
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder handler(Handler handler) {
      this.handler = handler;
      return this;
    }

    public io.rong.imageloader.core.DisplayImageOptions.Builder cloneFrom(io.rong.imageloader.core.DisplayImageOptions options) {
      this.imageResOnLoading = options.imageResOnLoading;
      this.imageResForEmptyUri = options.imageResForEmptyUri;
      this.imageResOnFail = options.imageResOnFail;
      this.imageOnLoading = options.imageOnLoading;
      this.imageForEmptyUri = options.imageForEmptyUri;
      this.imageOnFail = options.imageOnFail;
      this.resetViewBeforeLoading = options.resetViewBeforeLoading;
      this.cacheInMemory = options.cacheInMemory;
      this.cacheOnDisk = options.cacheOnDisk;
      this.imageScaleType = options.imageScaleType;
      this.decodingOptions = options.decodingOptions;
      this.delayBeforeLoading = options.delayBeforeLoading;
      this.considerExifParams = options.considerExifParams;
      this.extraForDownloader = options.extraForDownloader;
      this.preProcessor = options.preProcessor;
      this.postProcessor = options.postProcessor;
      this.displayer = options.displayer;
      this.handler = options.handler;
      this.isSyncLoading = options.isSyncLoading;
      return this;
    }

    public io.rong.imageloader.core.DisplayImageOptions build() {
      return new io.rong.imageloader.core.DisplayImageOptions(this);
    }
  }
}
