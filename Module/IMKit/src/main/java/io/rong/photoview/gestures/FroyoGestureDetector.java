//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.photoview.gestures;

import android.annotation.TargetApi;
import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

import io.rong.photoview.gestures.EclairGestureDetector;

@TargetApi(8)
public class FroyoGestureDetector extends EclairGestureDetector {
  protected final ScaleGestureDetector mDetector;

  public FroyoGestureDetector(Context context) {
    super(context);
    OnScaleGestureListener mScaleListener = new OnScaleGestureListener() {
      public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        if (!Float.isNaN(scaleFactor) && !Float.isInfinite(scaleFactor)) {
          io.rong.photoview.gestures.FroyoGestureDetector.this.mListener.onScale(scaleFactor, detector.getFocusX(), detector.getFocusY());
          return true;
        } else {
          return false;
        }
      }

      public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
      }

      public void onScaleEnd(ScaleGestureDetector detector) {
      }
    };
    this.mDetector = new ScaleGestureDetector(context, mScaleListener);
  }

  public boolean isScaling() {
    return this.mDetector.isInProgress();
  }

  public boolean onTouchEvent(MotionEvent ev) {
    try {
      this.mDetector.onTouchEvent(ev);
      return super.onTouchEvent(ev);
    } catch (IllegalArgumentException var3) {
      return true;
    }
  }
}
