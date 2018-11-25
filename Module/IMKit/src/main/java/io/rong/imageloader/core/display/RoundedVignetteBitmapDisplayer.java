//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core.display;

import android.graphics.Bitmap;
import android.graphics.ComposeShader;
import android.graphics.Matrix;
import android.graphics.PorterDuff.Mode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;

import io.rong.imageloader.core.assist.LoadedFrom;
import io.rong.imageloader.core.display.RoundedBitmapDisplayer;
import io.rong.imageloader.core.imageaware.ImageAware;
import io.rong.imageloader.core.imageaware.ImageViewAware;

public class RoundedVignetteBitmapDisplayer extends RoundedBitmapDisplayer {
  public RoundedVignetteBitmapDisplayer(int cornerRadiusPixels, int marginPixels) {
    super(cornerRadiusPixels, marginPixels);
  }

  public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
    if (!(imageAware instanceof ImageViewAware)) {
      throw new IllegalArgumentException("ImageAware should wrap ImageView. ImageViewAware is expected.");
    } else {
      imageAware.setImageDrawable(new io.rong.imageloader.core.display.RoundedVignetteBitmapDisplayer.RoundedVignetteDrawable(bitmap, this.cornerRadius, this.margin));
    }
  }

  protected static class RoundedVignetteDrawable extends RoundedDrawable {
    RoundedVignetteDrawable(Bitmap bitmap, int cornerRadius, int margin) {
      super(bitmap, cornerRadius, margin);
    }

    protected void onBoundsChange(Rect bounds) {
      super.onBoundsChange(bounds);
      RadialGradient vignette = new RadialGradient(this.mRect.centerX(), this.mRect.centerY() * 1.0F / 0.7F, this.mRect.centerX() * 1.3F, new int[]{0, 0, 2130706432}, new float[]{0.0F, 0.7F, 1.0F}, TileMode.CLAMP);
      Matrix oval = new Matrix();
      oval.setScale(1.0F, 0.7F);
      vignette.setLocalMatrix(oval);
      this.paint.setShader(new ComposeShader(this.bitmapShader, vignette, Mode.SRC_OVER));
    }
  }
}
