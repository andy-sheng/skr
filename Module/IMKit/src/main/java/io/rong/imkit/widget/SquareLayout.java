//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class SquareLayout extends RelativeLayout {
  public SquareLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    this.setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
    heightMeasureSpec = widthMeasureSpec = MeasureSpec.makeMeasureSpec(this.getMeasuredWidth(), 1073741824);
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }
}
