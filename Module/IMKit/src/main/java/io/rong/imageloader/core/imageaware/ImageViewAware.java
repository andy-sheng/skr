//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core.imageaware;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import java.lang.reflect.Field;

import io.rong.imageloader.core.assist.ViewScaleType;
import io.rong.imageloader.core.imageaware.ViewAware;
import io.rong.imageloader.utils.L;

public class ImageViewAware extends ViewAware {
  public ImageViewAware(ImageView imageView) {
    super(imageView);
  }

  public ImageViewAware(ImageView imageView, boolean checkActualViewSize) {
    super(imageView, checkActualViewSize);
  }

  public int getWidth() {
    int width = super.getWidth();
    if (width <= 0) {
      ImageView imageView = (ImageView)this.viewRef.get();
      if (imageView != null) {
        width = getImageViewFieldValue(imageView, "mMaxWidth");
      }
    }

    return width;
  }

  public int getHeight() {
    int height = super.getHeight();
    if (height <= 0) {
      ImageView imageView = (ImageView)this.viewRef.get();
      if (imageView != null) {
        height = getImageViewFieldValue(imageView, "mMaxHeight");
      }
    }

    return height;
  }

  public ViewScaleType getScaleType() {
    ImageView imageView = (ImageView)this.viewRef.get();
    return imageView != null ? ViewScaleType.fromImageView(imageView) : super.getScaleType();
  }

  public ImageView getWrappedView() {
    return (ImageView)super.getWrappedView();
  }

  protected void setImageDrawableInto(Drawable drawable, View view) {
    ((ImageView)view).setImageDrawable(drawable);
    if (drawable instanceof AnimationDrawable) {
      ((AnimationDrawable)drawable).start();
    }

  }

  protected void setImageBitmapInto(Bitmap bitmap, View view) {
    ((ImageView)view).setImageBitmap(bitmap);
  }

  private static int getImageViewFieldValue(Object object, String fieldName) {
    int value = 0;

    try {
      Field field = ImageView.class.getDeclaredField(fieldName);
      field.setAccessible(true);
      int fieldValue = (Integer)field.get(object);
      if (fieldValue > 0 && fieldValue < 2147483647) {
        value = fieldValue;
      }
    } catch (Exception var5) {
      L.e(var5);
    }

    return value;
  }
}
