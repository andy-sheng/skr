package com.module.playways.rank.song.holder;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class SongCardRecycleView extends RecyclerView {

    public final static String TAG = "SongCardRecycleView";

    public SongCardRecycleView(Context context) {
        super(context);
    }

    public SongCardRecycleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SongCardRecycleView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        MyLog.d(TAG, "onInterceptTouchEvent" + " ev=" + ev.getAction()+" begin");
//        boolean t = super.onInterceptTouchEvent(ev);
        boolean t = false;
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
//        boolean t = super.onTouchEvent(ev);
        boolean t = false;
//        MyLog.d(TAG, "onTouchEvent" + " ev=" + ev.getAction()+" r="+t);
        return t;
    }
}
