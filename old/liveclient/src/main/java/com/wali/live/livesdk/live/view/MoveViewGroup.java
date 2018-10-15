package com.wali.live.livesdk.live.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.base.global.GlobalData;

/**
 * Created by Star on 16/10/17.
 */
public class MoveViewGroup extends LinearLayout {
    private final static String TAG = MoveViewGroup.class.getSimpleName();

    boolean mIsInTouchMode = false;

    private int mLastX, mLastY;

    private int mMinLeft = 0;
    private int mMinTop = 0;
    private int mMaxRight = 0;
    private int mMaxBottom = 0;

    public int mScreenWidth = GlobalData.screenWidth;
    public int mScreenHeight = GlobalData.screenHeight;

    public MoveViewGroup(Context context) {
        this(context, null);
    }

    public MoveViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MoveViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) event.getRawX();// 获取触摸事件触摸位置的原始X坐标
                mLastY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) event.getRawX() - mLastX;
                int dy = (int) event.getRawY() - mLastY;
                if (!mIsInTouchMode) {
                    if (Math.abs(dx) < 3 && Math.abs(dy) < 3) {
                        mIsInTouchMode = false;
                    } else {
                        mIsInTouchMode = true;
                    }
                }
                MarginLayoutParams params = (MarginLayoutParams) this.getLayoutParams();
                if (params.leftMargin + dx + this.getWidth() < mMaxRight && params.leftMargin + dx > mMinLeft) {
                    params.leftMargin += dx;
                }
                if (params.topMargin + dy + this.getHeight() < mMaxBottom && params.topMargin + dy > mMinTop) {
                    params.topMargin += dy;
                }

                this.setLayoutParams(params);
                mLastX = (int) event.getRawX();
                mLastY = (int) event.getRawY();
                this.postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (mIsInTouchMode) {
                    mIsInTouchMode = false;
                    int count = this.getChildCount();
                    for (int i = 0; i < count; i++) {
                        View view = this.getChildAt(i);
                        view.setPressed(false);
                    }
                    return true;
                }
                break;
            default:
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    public void resetMoveRound(boolean isLandscape) {
        if (isLandscape) {
            mMaxRight = mScreenHeight;
            mMaxBottom = mScreenWidth;
        } else {
            mMaxRight = mScreenWidth;
            mMaxBottom = mScreenHeight;
        }
    }
}
