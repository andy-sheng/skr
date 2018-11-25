//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Layout;
import android.util.AttributeSet;
import android.widget.TextView;

import io.rong.imkit.R;

public class AutoLinkTextView extends TextView {
  private int mMaxWidth;

  public AutoLinkTextView(Context context) {
    super(context);
  }

  public AutoLinkTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.initWidth(context, attrs);
    this.setAutoLinkMask(7);
  }

  public AutoLinkTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    this.initWidth(context, attrs);
    this.setAutoLinkMask(7);
  }

  @TargetApi(21)
  public AutoLinkTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    this.initWidth(context, attrs);
    this.setAutoLinkMask(7);
  }

  private void initWidth(Context context, AttributeSet attrs) {
    TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AutoLinkTextView);
    this.mMaxWidth = array.getDimensionPixelSize(R.styleable.AutoLinkTextView_RCMaxWidth, 0);
    this.setMaxWidth(this.mMaxWidth);
    array.recycle();
  }

  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    Layout layout = this.getLayout();
    float width = 0.0F;

    for(int i = 0; i < layout.getLineCount(); ++i) {
      width = Math.max(width, layout.getLineWidth(i));
    }

    width += (float)(this.getCompoundPaddingLeft() + this.getCompoundPaddingRight());
    if (this.getBackground() != null) {
      width = Math.max(width, (float)this.getBackground().getIntrinsicWidth());
    }

    if (this.mMaxWidth != 0) {
      width = Math.min(width, (float)this.mMaxWidth);
    }

    this.setMeasuredDimension((int)Math.ceil((double)width), this.getMeasuredHeight());
  }
}
