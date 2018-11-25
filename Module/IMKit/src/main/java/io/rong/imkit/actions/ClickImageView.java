//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.actions;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import io.rong.imkit.R;

public class ClickImageView extends RelativeLayout {
  private ImageView imageView;

  public ClickImageView(Context context) {
    super(context);
    this.initView(context);
  }

  public ClickImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.initView(context);
  }

  private void initView(Context context) {
    this.imageView = new ImageView(context);
    int width = context.getResources().getDimensionPixelSize(R.dimen.rc_ext_more_imgage_width);
    int height = context.getResources().getDimensionPixelOffset(R.dimen.rc_ext_more_imgage_height);
    this.imageView.setLayoutParams(new LayoutParams(width, height));
    LayoutParams params = new LayoutParams(-2, -2);
    params.addRule(13);
    this.addView(this.imageView, params);
  }

  public void setImageDrawable(Drawable drawable) {
    this.imageView.setImageDrawable(drawable);
  }

  public void setEnable(boolean enable) {
    this.imageView.setEnabled(enable);
  }
}
