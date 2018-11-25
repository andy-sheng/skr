//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.model;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

public class RongGridView extends GridView {
  public RongGridView(Context context) {
    super(context);
  }

  public RongGridView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public RongGridView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public boolean dispatchTouchEvent(MotionEvent ev) {
    return super.dispatchTouchEvent(ev);
  }

  public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // noSrcollGridView
    int expandSpec = MeasureSpec.makeMeasureSpec(536870911,   MeasureSpec.AT_MOST);
    super.onMeasure(widthMeasureSpec, expandSpec);
  }
}
