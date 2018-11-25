//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.photoview;

import android.graphics.RectF;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;
import android.widget.ImageView;

import io.rong.photoview.PhotoViewAttacher;

public class DefaultOnDoubleTapListener implements OnDoubleTapListener {
  private PhotoViewAttacher photoViewAttacher;

  public DefaultOnDoubleTapListener(PhotoViewAttacher photoViewAttacher) {
    this.setPhotoViewAttacher(photoViewAttacher);
  }

  public void setPhotoViewAttacher(PhotoViewAttacher newPhotoViewAttacher) {
    this.photoViewAttacher = newPhotoViewAttacher;
  }

  public boolean onSingleTapConfirmed(MotionEvent e) {
    if (this.photoViewAttacher == null) {
      return false;
    } else {
      ImageView imageView = this.photoViewAttacher.getImageView();
      if (null != this.photoViewAttacher.getOnPhotoTapListener()) {
        RectF displayRect = this.photoViewAttacher.getDisplayRect();
        if (null != displayRect) {
          float x = e.getX();
          float y = e.getY();
          float xResult = (x - displayRect.left) / displayRect.width();
          float yResult = (y - displayRect.top) / displayRect.height();
          this.photoViewAttacher.getOnPhotoTapListener().onPhotoTap(imageView, xResult, yResult);
          return true;
        }
      }

      if (null != this.photoViewAttacher.getOnViewTapListener()) {
        this.photoViewAttacher.getOnViewTapListener().onViewTap(imageView, e.getX(), e.getY());
      }

      return false;
    }
  }

  public boolean onDoubleTap(MotionEvent ev) {
    if (this.photoViewAttacher == null) {
      return false;
    } else {
      try {
        float scale = this.photoViewAttacher.getScale();
        float x = ev.getX();
        float y = ev.getY();
        if (scale < this.photoViewAttacher.getMediumScale()) {
          this.photoViewAttacher.setScale(this.photoViewAttacher.getMediumScale(), x, y, true);
        } else if (scale >= this.photoViewAttacher.getMediumScale() && scale < this.photoViewAttacher.getMaximumScale()) {
          this.photoViewAttacher.setScale(this.photoViewAttacher.getMaximumScale(), x, y, true);
        } else {
          this.photoViewAttacher.setScale(this.photoViewAttacher.getMinimumScale(), x, y, true);
        }
      } catch (ArrayIndexOutOfBoundsException var5) {
        ;
      }

      return true;
    }
  }

  public boolean onDoubleTapEvent(MotionEvent e) {
    return false;
  }
}
