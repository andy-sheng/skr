//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core.display;

import android.graphics.Bitmap;

import io.rong.imageloader.core.assist.LoadedFrom;
import io.rong.imageloader.core.display.BitmapDisplayer;
import io.rong.imageloader.core.imageaware.ImageAware;

public final class SimpleBitmapDisplayer implements BitmapDisplayer {
  public SimpleBitmapDisplayer() {
  }

  public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
    imageAware.setImageBitmap(bitmap);
  }
}
