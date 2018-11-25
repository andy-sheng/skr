//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;

import java.io.IOException;
import java.io.InputStream;

import io.rong.imageloader.core.assist.ImageScaleType;
import io.rong.imageloader.core.assist.ImageSize;
import io.rong.imageloader.core.decode.ImageDecoder;
import io.rong.imageloader.core.decode.ImageDecodingInfo;
import io.rong.imageloader.core.download.ImageDownloader.Scheme;
import io.rong.imageloader.utils.ImageSizeUtils;
import io.rong.imageloader.utils.IoUtils;
import io.rong.imageloader.utils.L;

public class BaseImageDecoder implements ImageDecoder {
  protected static final String LOG_SUBSAMPLE_IMAGE = "Subsample original image (%1$s) to %2$s (scale = %3$d) [%4$s]";
  protected static final String LOG_SCALE_IMAGE = "Scale subsampled image (%1$s) to %2$s (scale = %3$.5f) [%4$s]";
  protected static final String LOG_ROTATE_IMAGE = "Rotate image on %1$d° [%2$s]";
  protected static final String LOG_FLIP_IMAGE = "Flip image horizontally [%s]";
  protected static final String ERROR_NO_IMAGE_STREAM = "No stream for image [%s]";
  protected static final String ERROR_CANT_DECODE_IMAGE = "Image can't be decoded [%s]";
  protected final boolean loggingEnabled;

  public BaseImageDecoder(boolean loggingEnabled) {
    this.loggingEnabled = loggingEnabled;
  }

  public Bitmap decode(ImageDecodingInfo decodingInfo) throws IOException {
    InputStream imageStream = this.getImageStream(decodingInfo);
    if (imageStream == null) {
      L.e("No stream for image [%s]", new Object[]{decodingInfo.getImageKey()});
      return null;
    } else {
      Bitmap decodedBitmap;
      io.rong.imageloader.core.decode.BaseImageDecoder.ImageFileInfo imageInfo;
      try {
        imageInfo = this.defineImageSizeAndRotation(imageStream, decodingInfo);
        imageStream = this.resetStream(imageStream, decodingInfo);
        Options decodingOptions = this.prepareDecodingOptions(imageInfo.imageSize, decodingInfo);
        decodedBitmap = BitmapFactory.decodeStream(imageStream, (Rect)null, decodingOptions);
      } finally {
        IoUtils.closeSilently(imageStream);
      }

      if (decodedBitmap == null) {
        L.e("Image can't be decoded [%s]", new Object[]{decodingInfo.getImageKey()});
      } else {
        decodedBitmap = this.considerExactScaleAndOrientatiton(decodedBitmap, decodingInfo, imageInfo.exif.rotation, imageInfo.exif.flipHorizontal);
      }

      return decodedBitmap;
    }
  }

  protected InputStream getImageStream(ImageDecodingInfo decodingInfo) throws IOException {
    return decodingInfo.getDownloader().getStream(decodingInfo.getImageUri(), decodingInfo.getExtraForDownloader());
  }

  protected io.rong.imageloader.core.decode.BaseImageDecoder.ImageFileInfo defineImageSizeAndRotation(InputStream imageStream, ImageDecodingInfo decodingInfo) throws IOException {
    Options options = new Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeStream(imageStream, (Rect)null, options);
    String imageUri = decodingInfo.getImageUri();
    io.rong.imageloader.core.decode.BaseImageDecoder.ExifInfo exif;
    if (decodingInfo.shouldConsiderExifParams() && this.canDefineExifParams(imageUri, options.outMimeType)) {
      exif = this.defineExifOrientation(imageUri);
    } else {
      exif = new io.rong.imageloader.core.decode.BaseImageDecoder.ExifInfo();
    }

    return new io.rong.imageloader.core.decode.BaseImageDecoder.ImageFileInfo(new ImageSize(options.outWidth, options.outHeight, exif.rotation), exif);
  }

  private boolean canDefineExifParams(String imageUri, String mimeType) {
    return "image/jpeg".equalsIgnoreCase(mimeType) && Scheme.ofUri(imageUri) == Scheme.FILE;
  }

  protected io.rong.imageloader.core.decode.BaseImageDecoder.ExifInfo defineExifOrientation(String imageUri) {
    int rotation = 0;
    boolean flip = false;

    try {
      ExifInterface exif = new ExifInterface(Scheme.FILE.crop(imageUri));
      int exifOrientation = exif.getAttributeInt("Orientation", 1);
      switch(exifOrientation) {
        case 2:
          flip = true;
        case 1:
          rotation = 0;
          break;
        case 4:
          flip = true;
        case 3:
          rotation = 180;
          break;
        case 5:
          flip = true;
        case 8:
          rotation = 270;
          break;
        case 7:
          flip = true;
        case 6:
          rotation = 90;
      }
    } catch (IOException var6) {
      L.w("Can't read EXIF tags from file [%s]", new Object[]{imageUri});
    }

    return new io.rong.imageloader.core.decode.BaseImageDecoder.ExifInfo(rotation, flip);
  }

  protected Options prepareDecodingOptions(ImageSize imageSize, ImageDecodingInfo decodingInfo) {
    ImageScaleType scaleType = decodingInfo.getImageScaleType();
    int scale;
    if (scaleType == ImageScaleType.NONE) {
      scale = 1;
    } else if (scaleType == ImageScaleType.NONE_SAFE) {
      scale = ImageSizeUtils.computeMinImageSampleSize(imageSize);
    } else {
      ImageSize targetSize = decodingInfo.getTargetSize();
      boolean powerOf2 = scaleType == ImageScaleType.IN_SAMPLE_POWER_OF_2;
      scale = ImageSizeUtils.computeImageSampleSize(imageSize, targetSize, decodingInfo.getViewScaleType(), powerOf2);
    }

    if (scale > 1 && this.loggingEnabled) {
      L.d("Subsample original image (%1$s) to %2$s (scale = %3$d) [%4$s]", new Object[]{imageSize, imageSize.scaleDown(scale), scale, decodingInfo.getImageKey()});
    }

    Options decodingOptions = decodingInfo.getDecodingOptions();
    decodingOptions.inSampleSize = scale;
    return decodingOptions;
  }

  protected InputStream resetStream(InputStream imageStream, ImageDecodingInfo decodingInfo) throws IOException {
    if (imageStream.markSupported()) {
      try {
        imageStream.reset();
        return imageStream;
      } catch (IOException var4) {
        ;
      }
    }

    IoUtils.closeSilently(imageStream);
    return this.getImageStream(decodingInfo);
  }

  protected Bitmap considerExactScaleAndOrientatiton(Bitmap subsampledBitmap, ImageDecodingInfo decodingInfo, int rotation, boolean flipHorizontal) {
    Matrix m = new Matrix();
    ImageScaleType scaleType = decodingInfo.getImageScaleType();
    if (scaleType == ImageScaleType.EXACTLY || scaleType == ImageScaleType.EXACTLY_STRETCHED) {
      ImageSize srcSize = new ImageSize(subsampledBitmap.getWidth(), subsampledBitmap.getHeight(), rotation);
      float scale = ImageSizeUtils.computeImageScale(srcSize, decodingInfo.getTargetSize(), decodingInfo.getViewScaleType(), scaleType == ImageScaleType.EXACTLY_STRETCHED);
      if (Float.compare(scale, 1.0F) != 0) {
        m.setScale(scale, scale);
        if (this.loggingEnabled) {
          L.d("Scale subsampled image (%1$s) to %2$s (scale = %3$.5f) [%4$s]", new Object[]{srcSize, srcSize.scale(scale), scale, decodingInfo.getImageKey()});
        }
      }
    }

    if (flipHorizontal) {
      m.postScale(-1.0F, 1.0F);
      if (this.loggingEnabled) {
        L.d("Flip image horizontally [%s]", new Object[]{decodingInfo.getImageKey()});
      }
    }

    if (rotation != 0) {
      m.postRotate((float)rotation);
      if (this.loggingEnabled) {
        L.d("Rotate image on %1$d° [%2$s]", new Object[]{rotation, decodingInfo.getImageKey()});
      }
    }

    Bitmap finalBitmap = Bitmap.createBitmap(subsampledBitmap, 0, 0, subsampledBitmap.getWidth(), subsampledBitmap.getHeight(), m, true);
    if (finalBitmap != subsampledBitmap) {
      subsampledBitmap.recycle();
    }

    return finalBitmap;
  }

  protected static class ImageFileInfo {
    public final ImageSize imageSize;
    public final io.rong.imageloader.core.decode.BaseImageDecoder.ExifInfo exif;

    protected ImageFileInfo(ImageSize imageSize, io.rong.imageloader.core.decode.BaseImageDecoder.ExifInfo exif) {
      this.imageSize = imageSize;
      this.exif = exif;
    }
  }

  protected static class ExifInfo {
    public final int rotation;
    public final boolean flipHorizontal;

    protected ExifInfo() {
      this.rotation = 0;
      this.flipHorizontal = false;
    }

    protected ExifInfo(int rotation, boolean flipHorizontal) {
      this.rotation = rotation;
      this.flipHorizontal = flipHorizontal;
    }
  }
}
