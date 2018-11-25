//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.actions;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.rong.imkit.actions.ClickImageView;
import io.rong.imkit.actions.IClickActions;

public class MoreActionLayout extends ViewGroup {
  private Context context;
  private Fragment fragment;
  private List<IClickActions> clickActions;

  public MoreActionLayout(Context context) {
    super(context);
    this.context = context;
  }

  public MoreActionLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
  }

  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int totalWidth = MeasureSpec.getSize(widthMeasureSpec);
    int childWidth = 0;
    if (this.getChildCount() > 0) {
      childWidth = totalWidth / this.getChildCount();
    }

    for(int i = 0; i < this.getChildCount(); ++i) {
      int childWidthSpec = MeasureSpec.makeMeasureSpec(childWidth,  MeasureSpec.EXACTLY);
      View childView = this.getChildAt(i);
      childView.measure(childWidthSpec, heightMeasureSpec);
    }

  }

  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    int total = this.getMeasuredWidth();
    int height = this.getMeasuredHeight();
    int width = 0;
    if (this.getChildCount() > 0) {
      width = total / this.getChildCount();
    }

    for(int i = 0; i < this.getChildCount(); ++i) {
      View child = this.getChildAt(i);
      child.layout(i * width, 0, (i + 1) * width, height);
    }

  }

  public void setFragment(Fragment fragment) {
    this.fragment = fragment;
  }

  public void addActions(List<IClickActions> actions) {
    this.clickActions = actions;
    if (actions != null && actions.size() > 0) {
      for(int i = 0; i < actions.size(); ++i) {
        final IClickActions action = (IClickActions)actions.get(i);
        ClickImageView view = new ClickImageView(this.context);
        view.setImageDrawable(((IClickActions)actions.get(i)).obtainDrawable(this.context));
        view.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
            action.onClick(io.rong.imkit.actions.MoreActionLayout.this.fragment);
          }
        });
        this.addView(view, i);
      }

      this.invalidate();
    }

  }

  public void refreshView(boolean enable) {
    for(int i = 0; i < this.getChildCount(); ++i) {
      ClickImageView view = (ClickImageView)this.getChildAt(i);
      view.setEnable(enable);
    }

  }
}
