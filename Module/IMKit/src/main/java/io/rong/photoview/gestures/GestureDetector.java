//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.photoview.gestures;

import android.view.MotionEvent;

import io.rong.photoview.gestures.OnGestureListener;

public interface GestureDetector {
  boolean onTouchEvent(MotionEvent var1);

  boolean isScaling();

  boolean isDragging();

  void setOnGestureListener(OnGestureListener var1);
}
