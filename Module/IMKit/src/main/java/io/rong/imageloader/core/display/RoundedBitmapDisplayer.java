//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core.display;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;

import io.rong.imageloader.core.assist.LoadedFrom;
import io.rong.imageloader.core.display.BitmapDisplayer;
import io.rong.imageloader.core.imageaware.ImageAware;
import io.rong.imageloader.core.imageaware.ImageViewAware;

public class RoundedBitmapDisplayer implements BitmapDisplayer {
  protected final int cornerRadius;
  protected final int margin;

  public RoundedBitmapDisplayer(int cornerRadiusPixels) {
    this(cornerRadiusPixels, 0);
  }

  public RoundedBitmapDisplayer(int cornerRadiusPixels, int marginPixels) {
    this.cornerRadius = cornerRadiusPixels;
    this.margin = marginPixels;
  }

  public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
    if (!(imageAware instanceof ImageViewAware)) {
      throw new IllegalArgumentException("ImageAware should wrap ImageView. ImageViewAware is expected.");
    } else {
      imageAware.setImageDrawable(new io.rong.imageloader.core.display.RoundedBitmapDisplayer.RoundedDrawable(bitmap, this.cornerRadius, this.margin));
    }
  }

  public static class RoundedDrawable extends Drawable {
    protected final float cornerRadius;
    protected final int margin;
    protected final RectF mRect = new RectF();
    protected final RectF mBitmapRect;
    protected final BitmapShader bitmapShader;
    protected final Paint paint;

    public RoundedDrawable(Bitmap bitmap, int cornerRadius, int margin) {
      this.cornerRadius = (float)cornerRadius;
      this.margin = margin;
      this.bitmapShader = new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP);
      this.mBitmapRect = new RectF((float)margin, (float)margin, (float)(bitmap.getWidth() - margin), (float)(bitmap.getHeight() - margin));
      this.paint = new Paint();
      this.paint.setAntiAlias(true);
      this.paint.setShader(this.bitmapShader);
      this.paint.setFilterBitmap(true);
      this.paint.setDither(true);
    }

    protected void onBoundsChange(Rect bounds) {
      super.onBoundsChange(bounds);
      this.mRect.set((float)this.margin, (float)this.margin, (float)(bounds.width() - this.margin), (float)(bounds.height() - this.margin));
      Matrix shaderMatrix = new Matrix();
      shaderMatrix.setRectToRect(this.mBitmapRect, this.mRect, ScaleToFit.FILL);
      this.bitmapShader.setLocalMatrix(shaderMatrix);
    }

    public void draw(Canvas canvas) {
      canvas.drawRoundRect(this.mRect, this.cornerRadius, this.cornerRadius, this.paint);
    }

    public int getOpacity() {
      return -3;
    }

    public void setAlpha(int alpha) {
      this.paint.setAlpha(alpha);
    }

    public void setColorFilter(ColorFilter cf) {
      this.paint.setColorFilter(cf);
    }
  }
}
