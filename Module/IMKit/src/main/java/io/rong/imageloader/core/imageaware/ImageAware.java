//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core.imageaware;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;

import io.rong.imageloader.core.assist.ViewScaleType;

public interface ImageAware {
  int getWidth();

  int getHeight();

  ViewScaleType getScaleType();

  View getWrappedView();

  boolean isCollected();

  int getId();

  boolean setImageDrawable(Drawable var1);

  boolean setImageBitmap(Bitmap var1);
}
