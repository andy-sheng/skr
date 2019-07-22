package com.common.view.ex;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.common.log.MyLog;

public class ExLinearLayout extends LinearLayout {
    public final String TAG = "ExLinearLayout";

    public ExLinearLayout(Context context) {
        super(context);
    }

    public ExLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadAttributes(context, attrs);
    }

    public ExLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadAttributes(context, attrs);
    }

    private void loadAttributes(Context context, AttributeSet attrs) {
        AttributeInject.injectBackground(this, context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        MyLog.d(TAG, "onInterceptTouchEvent" + " ev=" + ev.getAction()+" begin");
        boolean t = super.onInterceptTouchEvent(ev);
//        MyLog.d(TAG, "onInterceptTouchEvent" + " ev=" + ev.getAction()+" r="+t);
        return t;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
//        MyLog.d(TAG, "dispatchTouchEvent" + " ev=" + ev.getAction()+" begin");
        boolean t = super.dispatchTouchEvent(ev);
//        MyLog.d(TAG, "dispatchTouchEvent" + " ev=" + ev.getAction()+" r="+t);
        return t;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//        MyLog.d(TAG, "onTouchEvent" + " ev=" + ev.getAction()+" begin");
        boolean t = super.onTouchEvent(ev);
//        MyLog.d(TAG, "onTouchEvent" + " ev=" + ev.getAction()+" r="+t);
        return t;
    }
}
