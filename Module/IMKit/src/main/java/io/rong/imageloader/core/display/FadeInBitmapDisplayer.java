//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core.display;

import android.graphics.Bitmap;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;

import io.rong.imageloader.core.assist.LoadedFrom;
import io.rong.imageloader.core.display.BitmapDisplayer;
import io.rong.imageloader.core.imageaware.ImageAware;

public class FadeInBitmapDisplayer implements BitmapDisplayer {
  private final int durationMillis;
  private final boolean animateFromNetwork;
  private final boolean animateFromDisk;
  private final boolean animateFromMemory;

  public FadeInBitmapDisplayer(int durationMillis) {
    this(durationMillis, true, true, true);
  }

  public FadeInBitmapDisplayer(int durationMillis, boolean animateFromNetwork, boolean animateFromDisk, boolean animateFromMemory) {
    this.durationMillis = durationMillis;
    this.animateFromNetwork = animateFromNetwork;
    this.animateFromDisk = animateFromDisk;
    this.animateFromMemory = animateFromMemory;
  }

  public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
    imageAware.setImageBitmap(bitmap);
    if (this.animateFromNetwork && loadedFrom == LoadedFrom.NETWORK || this.animateFromDisk && loadedFrom == LoadedFrom.DISC_CACHE || this.animateFromMemory && loadedFrom == LoadedFrom.MEMORY_CACHE) {
      animate(imageAware.getWrappedView(), this.durationMillis);
    }

  }

  public static void animate(View imageView, int durationMillis) {
    if (imageView != null) {
      AlphaAnimation fadeImage = new AlphaAnimation(0.0F, 1.0F);
      fadeImage.setDuration((long)durationMillis);
      fadeImage.setInterpolator(new DecelerateInterpolator());
      imageView.startAnimation(fadeImage);
    }

  }
}
