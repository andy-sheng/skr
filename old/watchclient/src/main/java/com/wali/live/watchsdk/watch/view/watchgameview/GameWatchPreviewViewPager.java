package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class GameWatchPreviewViewPager extends ViewPager {

    int downX;
    long downTs;
    OnClickListener mListener;

    public GameWatchPreviewViewPager(Context context) {
        super(context);
    }

    public GameWatchPreviewViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downX = (int) ev.getX();
                downTs = System.currentTimeMillis();
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                downX = (int) ev.getX();
            }
            break;
            case MotionEvent.ACTION_UP: {
                int x = (int) ev.getX();
                if (Math.abs(downX - x) < 10 && System.currentTimeMillis() - downTs < 1000) {
                    if (mListener != null) {
                        mListener.onClick(null);
                    }
                }
            }
            break;
        }

        return super.onTouchEvent(ev);
    }


    public void setOutClickListener(OnClickListener l) {
        mListener = l;
    }
}
