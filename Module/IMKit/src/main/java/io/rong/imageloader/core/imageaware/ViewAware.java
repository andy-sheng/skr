//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core.imageaware;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import io.rong.imageloader.core.assist.ViewScaleType;
import io.rong.imageloader.core.imageaware.ImageAware;
import io.rong.imageloader.utils.L;

public abstract class ViewAware implements ImageAware {
  public static final String WARN_CANT_SET_DRAWABLE = "Can't set a drawable into view. You should call ImageLoader on UI thread for it.";
  public static final String WARN_CANT_SET_BITMAP = "Can't set a bitmap into view. You should call ImageLoader on UI thread for it.";
  protected Reference<View> viewRef;
  protected boolean checkActualViewSize;

  public ViewAware(View view) {
    this(view, true);
  }

  public ViewAware(View view, boolean checkActualViewSize) {
    if (view == null) {
      throw new IllegalArgumentException("view must not be null");
    } else {
      this.viewRef = new WeakReference(view);
      this.checkActualViewSize = checkActualViewSize;
    }
  }

  public int getWidth() {
    View view = (View)this.viewRef.get();
    if (view != null) {
      LayoutParams params = view.getLayoutParams();
      int width = 0;
      if (this.checkActualViewSize && params != null && params.width != -2) {
        width = view.getWidth();
      }

      if (width <= 0 && params != null) {
        width = params.width;
      }

      return width;
    } else {
      return 0;
    }
  }

  public int getHeight() {
    View view = (View)this.viewRef.get();
    if (view != null) {
      LayoutParams params = view.getLayoutParams();
      int height = 0;
      if (this.checkActualViewSize && params != null && params.height != -2) {
        height = view.getHeight();
      }

      if (height <= 0 && params != null) {
        height = params.height;
      }

      return height;
    } else {
      return 0;
    }
  }

  public ViewScaleType getScaleType() {
    return ViewScaleType.CROP;
  }

  public View getWrappedView() {
    return (View)this.viewRef.get();
  }

  public boolean isCollected() {
    return this.viewRef.get() == null;
  }

  public int getId() {
    View view = (View)this.viewRef.get();
    return view == null ? super.hashCode() : view.hashCode();
  }

  public boolean setImageDrawable(Drawable drawable) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
      View view = (View)this.viewRef.get();
      if (view != null) {
        this.setImageDrawableInto(drawable, view);
        return true;
      }
    } else {
      L.w("Can't set a drawable into view. You should call ImageLoader on UI thread for it.", new Object[0]);
    }

    return false;
  }

  public boolean setImageBitmap(Bitmap bitmap) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
      View view = (View)this.viewRef.get();
      if (view != null) {
        this.setImageBitmapInto(bitmap, view);
        return true;
      }
    } else {
      L.w("Can't set a bitmap into view. You should call ImageLoader on UI thread for it.", new Object[0]);
    }

    return false;
  }

  protected abstract void setImageDrawableInto(Drawable var1, View var2);

  protected abstract void setImageBitmapInto(Bitmap var1, View var2);
}
