package com.base.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by vera on 2018/5/28.
 * <p>
 * 解决多个ViewPager嵌套滑动冲突
 */

public class NestViewPager extends ViewPager {
    private boolean mCanScroll = true;

    public NestViewPager(@NonNull Context context) {
        super(context);
    }

    public NestViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (!mCanScroll) {
            return false;
        }
        if (v instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) v;
            final int scrollX = v.getScrollX();
            final int scrollY = v.getScrollY();
            final int count = group.getChildCount();
            for (int i = count - 1; i >= 0; i--) {
                final View child = group.getChildAt(i);
                if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight() &&
                        y + scrollY >= child.getTop() && y + scrollY < child.getBottom() &&
                        canScroll(child, true, dx, x + scrollX - child.getLeft(),
                                y + scrollY - child.getTop())) {
                    return true;
                }
            }
        }

        if (checkV) {
            if (v instanceof ViewPager) {
                return ((ViewPager) v).canScrollHorizontally(-dx);
            } else {
                return ViewCompat.canScrollHorizontally(v, -dx);
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        if (!mCanScroll) {
            return false;
        }
        return super.canScrollHorizontally(direction);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mCanScroll) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mCanScroll) {
            return false;
        }
        return super.onTouchEvent(ev);

    }

    public void setViewPagerCanScroll(boolean canScroll) {
        this.mCanScroll = canScroll;
    }
}
