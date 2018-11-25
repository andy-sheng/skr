//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import io.rong.imkit.widget.provider.IContainerItemProvider;

public class ProviderContainerView extends FrameLayout {
  Map<Class<? extends IContainerItemProvider>, AtomicInteger> mViewCounterMap;
  Map<Class<? extends IContainerItemProvider>, View> mContentViewMap;
  View mInflateView;
  int mMaxContainSize = 3;

  public ProviderContainerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    if (!this.isInEditMode()) {
      this.init(attrs);
    }

  }

  private void init(AttributeSet attrs) {
    this.mViewCounterMap = new HashMap();
    this.mContentViewMap = new HashMap();
  }

  public <T extends IContainerItemProvider> View inflate(T t) {
    View result = null;
    if (this.mInflateView != null) {
      this.mInflateView.setVisibility(8);
    }

    if (this.mContentViewMap.containsKey(t.getClass())) {
      result = (View)this.mContentViewMap.get(t.getClass());
      this.mInflateView = result;
      ((AtomicInteger)this.mViewCounterMap.get(t.getClass())).incrementAndGet();
    }

    if (result != null) {
      if (result.getVisibility() == 8) {
        result.setVisibility(0);
      }

      return result;
    } else {
      this.recycle();
      result = t.newView(this.getContext(), this);
      if (result != null) {
        super.addView(result);
        this.mContentViewMap.put(t.getClass(), result);
        this.mViewCounterMap.put(t.getClass(), new AtomicInteger());
      }

      this.mInflateView = result;
      return result;
    }
  }

  public View getCurrentInflateView() {
    return this.mInflateView;
  }

  public void containerViewLeft() {
    if (this.mInflateView != null) {
      LayoutParams params = (LayoutParams)this.mInflateView.getLayoutParams();
      params.gravity = 19;
    }
  }

  public void containerViewRight() {
    if (this.mInflateView != null) {
      LayoutParams params = (LayoutParams)this.mInflateView.getLayoutParams();
      params.gravity = 21;
    }
  }

  public void containerViewCenter() {
    if (this.mInflateView != null) {
      LayoutParams params = (LayoutParams)this.mInflateView.getLayoutParams();
      params.gravity = 17;
    }
  }

  private void recycle() {
    if (this.mInflateView != null) {
      int count = this.getChildCount();
      if (count >= this.mMaxContainSize) {
        Entry<Class<? extends IContainerItemProvider>, AtomicInteger> min = null;

        Entry item;
        for(Iterator var3 = this.mViewCounterMap.entrySet().iterator(); var3.hasNext(); min = ((AtomicInteger)min.getValue()).get() > ((AtomicInteger)item.getValue()).get() ? item : min) {
          item = (Entry)var3.next();
          if (min == null) {
            min = item;
          }
        }

        this.mViewCounterMap.remove(min.getKey());
        View view = (View)this.mContentViewMap.remove(min.getKey());
        this.removeView(view);
      }

    }
  }
}
