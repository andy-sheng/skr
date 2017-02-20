package com.wali.live.watchsdk.watch.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengsimin on 16/4/12.
 */
public class TouchDelegateView extends View {
    private static final String TAG = "TouchDelegateView";
    private List<View> mDispatchTouchDelegateList;
    private List<View> mOnTouchDelegateList;

    public TouchDelegateView(Context context) {
        super(context);
    }

    public TouchDelegateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchDelegateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mDispatchTouchDelegateList != null) {
            for (View v : mDispatchTouchDelegateList) {
                v.dispatchTouchEvent(event);
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mOnTouchDelegateList != null) {
            for (View v : mOnTouchDelegateList) {
                v.onTouchEvent(event);
            }
        }
        super.onTouchEvent(event);
        return true;
    }

    public void addDispatchTouchDelegate(View view) {
        if (mDispatchTouchDelegateList == null) {
            mDispatchTouchDelegateList = new ArrayList<>();
        }
        mDispatchTouchDelegateList.add(view);
    }

    public void removeDispatchTouchDelegate(View view) {
        if (mDispatchTouchDelegateList != null) {
            mDispatchTouchDelegateList.remove(view);
        }
    }

    public void addOnTouchDelegate(View view) {
        if (mOnTouchDelegateList == null) {
            mOnTouchDelegateList = new ArrayList<>();
        }
        mOnTouchDelegateList.add(view);
    }

    public static final int FLING_RIGHT_MIN_DISTANCE = 280;
    public static final int FLING_LEFT_MIN_DISTANCE = 100;
    private float mLastX, mLastY, mDownX, mDownY, mUpX, mUpY;
    private float mTranslateX = 0;
    private boolean mCanTouch = true;

    public void setGestureListener(final GestureListener l) {
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (l == null) {
                    return false;
                }
                if (!mCanTouch) {
                    return true;
                }
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: {
                        l.onDown();
                        mLastX = event.getX();
                        mLastY = event.getY();
                        mDownX = mLastX;
                        mDownY = mLastY;
                        mTranslateX = 0;
                    }
                    break;
                    case MotionEvent.ACTION_MOVE: {
                        if (Math.abs(event.getX() - mDownX) > Math.abs(event.getY() - mDownY)) {
                            float t = event.getX() - mLastX;
                            mTranslateX += t;
                            l.onMoveFilterX(t);
                        }
                        mLastX = event.getX();
                        mLastY = event.getY();
                    }
                    break;
                    case MotionEvent.ACTION_UP: {
                        l.onUp();
                        mUpX = event.getX();
                        mUpY = event.getY();
                        if (mTranslateX > FLING_RIGHT_MIN_DISTANCE) {
                            // 右滑
                            l.onRightFlingFilterX();
                        } else if (mTranslateX < -FLING_LEFT_MIN_DISTANCE) {
                            // 左滑
                            l.onLeftFlingFilterX();
                        } else {
                            l.onSingleTap();
                        }
                    }
                    break;
                    case MotionEvent.ACTION_CANCEL: {
                        l.onCancel();
                    }
                }
                return false;
            }
        });

    }

    public interface GestureListener {
        void onLeftFlingFilterX();

        void onRightFlingFilterX();

        void onSingleTap();

        boolean onDown();

        void onMoveFilterX(float tx);

        void onUp();

        void onCancel();
    }
}
