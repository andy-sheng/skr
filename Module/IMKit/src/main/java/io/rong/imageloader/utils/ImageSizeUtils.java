//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.utils;

import android.opengl.GLES10;

import io.rong.imageloader.core.assist.ImageSize;
import io.rong.imageloader.core.assist.ViewScaleType;
import io.rong.imageloader.core.imageaware.ImageAware;

public final class ImageSizeUtils {
  private static final int DEFAULT_MAX_BITMAP_DIMENSION = 2048;
  private static ImageSize maxBitmapSize;

  private ImageSizeUtils() {
  }

  public static ImageSize defineTargetSizeForView(ImageAware imageAware, ImageSize maxImageSize) {
    int width = imageAware.getWidth();
    if (width <= 0) {
      width = maxImageSize.getWidth();
    }

    int height = imageAware.getHeight();
    if (height <= 0) {
      height = maxImageSize.getHeight();
    }

    return new ImageSize(width, height);
  }

  public static int computeImageSampleSize(ImageSize srcSize, ImageSize targetSize, ViewScaleType viewScaleType, boolean powerOf2Scale) {
    int srcWidth;
    int srcHeight;
    int scale;
    srcWidth = srcSize.getWidth();
    srcHeight = srcSize.getHeight();
    int targetWidth = targetSize.getWidth();
    int targetHeight = targetSize.getHeight();
    scale = 1;
    int halfWidth;
    int halfHeight;
    label41:
    switch(viewScaleType) {
      case FIT_INSIDE:
        if (powerOf2Scale) {
          halfWidth = srcWidth / 2;
          halfHeight = srcHeight / 2;

          while(true) {
            if (halfWidth / scale <= targetWidth && halfHeight / scale <= targetHeight) {
              break label41;
            }

            scale *= 2;
          }
        } else {
          scale = Math.max(srcWidth / targetWidth, srcHeight / targetHeight);
          break;
        }
      case CROP:
        if (powerOf2Scale) {
          halfWidth = srcWidth / 2;

          for(halfHeight = srcHeight / 2; halfWidth / scale > targetWidth && halfHeight / scale > targetHeight; scale *= 2) {
            ;
          }
        } else {
          scale = Math.min(srcWidth / targetWidth, srcHeight / targetHeight);
        }
    }

    if (scale < 1) {
      scale = 1;
    }

    scale = considerMaxTextureSize(srcWidth, srcHeight, scale, powerOf2Scale);
    return scale;
  }

  private static int considerMaxTextureSize(int srcWidth, int srcHeight, int scale, boolean powerOf2) {
    int maxWidth = maxBitmapSize.getWidth();
    int maxHeight = maxBitmapSize.getHeight();

    while(srcWidth / scale > maxWidth || srcHeight / scale > maxHeight) {
      if (powerOf2) {
        scale *= 2;
      } else {
        ++scale;
      }
    }

    return scale;
  }

  public static int computeMinImageSampleSize(ImageSize srcSize) {
    int srcWidth = srcSize.getWidth();
    int srcHeight = srcSize.getHeight();
    int targetWidth = maxBitmapSize.getWidth();
    int targetHeight = maxBitmapSize.getHeight();
    int widthScale = (int)Math.ceil((double)((float)srcWidth / (float)targetWidth));
    int heightScale = (int)Math.ceil((double)((float)srcHeight / (float)targetHeight));
    return Math.max(widthScale, heightScale);
  }

  public static float computeImageScale(ImageSize srcSize, ImageSize targetSize, ViewScaleType viewScaleType, boolean stretch) {
    int srcWidth = srcSize.getWidth();
    int srcHeight = srcSize.getHeight();
    int targetWidth = targetSize.getWidth();
    int targetHeight = targetSize.getHeight();
    float widthScale = (float)srcWidth / (float)targetWidth;
    float heightScale = (float)srcHeight / (float)targetHeight;
    int destWidth;
    int destHeight;
    if ((viewScaleType != ViewScaleType.FIT_INSIDE || widthScale < heightScale) && (viewScaleType != ViewScaleType.CROP || widthScale >= heightScale)) {
      destWidth = (int)((float)srcWidth / heightScale);
      destHeight = targetHeight;
    } else {
      destWidth = targetWidth;
      destHeight = (int)((float)srcHeight / widthScale);
    }

    float scale = 1.0F;
    if (!stretch && destWidth < srcWidth && destHeight < srcHeight || stretch && destWidth != srcWidth && destHeight != srcHeight) {
      scale = (float)destWidth / (float)srcWidth;
    }

    return scale;
  }

  static {
    int[] maxTextureSize = new int[1];
    GLES10.glGetIntegerv(3379, maxTextureSize, 0);
    int maxBitmapDimension = Math.max(maxTextureSize[0], 2048);
    maxBitmapSize = new ImageSize(maxBitmapDimension, maxBitmapDimension);
  }
}
