//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core.decode;

import android.annotation.TargetApi;
import android.graphics.BitmapFactory.Options;
import android.os.Build.VERSION;

import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.assist.ImageScaleType;
import io.rong.imageloader.core.assist.ImageSize;
import io.rong.imageloader.core.assist.ViewScaleType;
import io.rong.imageloader.core.download.ImageDownloader;

public class ImageDecodingInfo {
  private final String imageKey;
  private final String imageUri;
  private final String originalImageUri;
  private final ImageSize targetSize;
  private final ImageScaleType imageScaleType;
  private final ViewScaleType viewScaleType;
  private final ImageDownloader downloader;
  private final Object extraForDownloader;
  private final boolean considerExifParams;
  private final Options decodingOptions;

  public ImageDecodingInfo(String imageKey, String imageUri, String originalImageUri, ImageSize targetSize, ViewScaleType viewScaleType, ImageDownloader downloader, DisplayImageOptions displayOptions) {
    this.imageKey = imageKey;
    this.imageUri = imageUri;
    this.originalImageUri = originalImageUri;
    this.targetSize = targetSize;
    this.imageScaleType = displayOptions.getImageScaleType();
    this.viewScaleType = viewScaleType;
    this.downloader = downloader;
    this.extraForDownloader = displayOptions.getExtraForDownloader();
    this.considerExifParams = displayOptions.isConsiderExifParams();
    this.decodingOptions = new Options();
    this.copyOptions(displayOptions.getDecodingOptions(), this.decodingOptions);
  }

  private void copyOptions(Options srcOptions, Options destOptions) {
    destOptions.inDensity = srcOptions.inDensity;
    destOptions.inDither = srcOptions.inDither;
    destOptions.inInputShareable = srcOptions.inInputShareable;
    destOptions.inJustDecodeBounds = srcOptions.inJustDecodeBounds;
    destOptions.inPreferredConfig = srcOptions.inPreferredConfig;
    destOptions.inPurgeable = srcOptions.inPurgeable;
    destOptions.inSampleSize = srcOptions.inSampleSize;
    destOptions.inScaled = srcOptions.inScaled;
    destOptions.inScreenDensity = srcOptions.inScreenDensity;
    destOptions.inTargetDensity = srcOptions.inTargetDensity;
    destOptions.inTempStorage = srcOptions.inTempStorage;
    if (VERSION.SDK_INT >= 10) {
      this.copyOptions10(srcOptions, destOptions);
    }

    if (VERSION.SDK_INT >= 11) {
      this.copyOptions11(srcOptions, destOptions);
    }

  }

  @TargetApi(10)
  private void copyOptions10(Options srcOptions, Options destOptions) {
    destOptions.inPreferQualityOverSpeed = srcOptions.inPreferQualityOverSpeed;
  }

  @TargetApi(11)
  private void copyOptions11(Options srcOptions, Options destOptions) {
    destOptions.inBitmap = srcOptions.inBitmap;
    destOptions.inMutable = srcOptions.inMutable;
  }

  public String getImageKey() {
    return this.imageKey;
  }

  public String getImageUri() {
    return this.imageUri;
  }

  public String getOriginalImageUri() {
    return this.originalImageUri;
  }

  public ImageSize getTargetSize() {
    return this.targetSize;
  }

  public ImageScaleType getImageScaleType() {
    return this.imageScaleType;
  }

  public ViewScaleType getViewScaleType() {
    return this.viewScaleType;
  }

  public ImageDownloader getDownloader() {
    return this.downloader;
  }

  public Object getExtraForDownloader() {
    return this.extraForDownloader;
  }

  public boolean shouldConsiderExifParams() {
    return this.considerExifParams;
  }

  public Options getDecodingOptions() {
    return this.decodingOptions;
  }
}
