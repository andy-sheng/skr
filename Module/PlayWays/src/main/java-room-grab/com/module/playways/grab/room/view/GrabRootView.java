package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.common.view.ex.ExRelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class GrabRootView extends ExRelativeLayout {
    List<OnTouchListener> mOnTouchListeners = new ArrayList<>();

    public GrabRootView(Context context) {
        super(context);
        init();
    }

    public GrabRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabRootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                for (OnTouchListener l : mOnTouchListeners) {
                    boolean r = l.onTouch(v, event);
                    if (r) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void addOnTouchListener(OnTouchListener l) {
        if (mOnTouchListeners.contains(l)) {
            mOnTouchListeners.remove(l);
        }
        mOnTouchListeners.add(l);
    }

    public void removeOnTouchListener(OnTouchListener l) {
        mOnTouchListeners.remove(l);
    }
}
