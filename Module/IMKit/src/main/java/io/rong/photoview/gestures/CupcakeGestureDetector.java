//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.photoview.gestures;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import io.rong.photoview.gestures.GestureDetector;
import io.rong.photoview.gestures.OnGestureListener;
import io.rong.photoview.log.LogManager;

public class CupcakeGestureDetector implements GestureDetector {
    protected OnGestureListener mListener;
    private static final String LOG_TAG = "CupcakeGestureDetector";
    float mLastTouchX;
    float mLastTouchY;
    final float mTouchSlop;
    final float mMinimumVelocity;
    private VelocityTracker mVelocityTracker;
    private boolean mIsDragging;

    public void setOnGestureListener(OnGestureListener listener) {
        this.mListener = listener;
    }

    public CupcakeGestureDetector(Context context) {
        ViewConfiguration configuration = ViewConfiguration.get(context);
        this.mMinimumVelocity = (float) configuration.getScaledMinimumFlingVelocity();
        this.mTouchSlop = (float) configuration.getScaledTouchSlop();
    }

    float getActiveX(MotionEvent ev) {
        return ev.getX();
    }

    float getActiveY(MotionEvent ev) {
        return ev.getY();
    }

    public boolean isScaling() {
        return false;
    }

    public boolean isDragging() {
        return this.mIsDragging;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        float x;
        float y;
        switch (ev.getAction()) {
            case 0:
                this.mVelocityTracker = VelocityTracker.obtain();
                if (null != this.mVelocityTracker) {
                    this.mVelocityTracker.addMovement(ev);
                } else {
                    LogManager.getLogger().i("CupcakeGestureDetector", "Velocity tracker is null");
                }

                this.mLastTouchX = this.getActiveX(ev);
                this.mLastTouchY = this.getActiveY(ev);
                this.mIsDragging = false;
                break;
            case 1:
                if (this.mIsDragging && null != this.mVelocityTracker) {
                    this.mLastTouchX = this.getActiveX(ev);
                    this.mLastTouchY = this.getActiveY(ev);
                    this.mVelocityTracker.addMovement(ev);
                    this.mVelocityTracker.computeCurrentVelocity(1000);
                    x = this.mVelocityTracker.getXVelocity();
                    y = this.mVelocityTracker.getYVelocity();
                    if (Math.max(Math.abs(x), Math.abs(y)) >= this.mMinimumVelocity) {
                        this.mListener.onFling(this.mLastTouchX, this.mLastTouchY, -x, -y);
                    }
                }

                if (null != this.mVelocityTracker) {
                    this.mVelocityTracker.recycle();
                    this.mVelocityTracker = null;
                }
                break;
            case 2:
                x = this.getActiveX(ev);
                y = this.getActiveY(ev);
                float dx = x - this.mLastTouchX;
                float dy = y - this.mLastTouchY;
                if (!this.mIsDragging) {
                    this.mIsDragging = Math.sqrt((double) (dx * dx + dy * dy)) >= (double) this.mTouchSlop;
                }

                if (this.mIsDragging) {
                    this.mListener.onDrag(dx, dy);
                    this.mLastTouchX = x;
                    this.mLastTouchY = y;
                    if (null != this.mVelocityTracker) {
                        this.mVelocityTracker.addMovement(ev);
                    }
                }
                break;
            case 3:
                if (null != this.mVelocityTracker) {
                    this.mVelocityTracker.recycle();
                    this.mVelocityTracker = null;
                }
        }

        return true;
    }
}
