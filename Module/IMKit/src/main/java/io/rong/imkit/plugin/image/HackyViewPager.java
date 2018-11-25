//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.plugin.image;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class HackyViewPager extends ViewPager {
  public HackyViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public boolean onInterceptTouchEvent(MotionEvent ev) {
    try {
      return super.onInterceptTouchEvent(ev);
    } catch (IllegalArgumentException var3) {
      var3.printStackTrace();
      return false;
    }
  }

  public boolean onTouchEvent(MotionEvent event) {
    return super.onTouchEvent(event);
  }
}
