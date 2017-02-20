package com.base.image.fresco.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;

import com.base.log.MyLog;

/**
 * Created by lan on 2016/9/25.
 */

public class MovableImageView extends TintableImageView {
    public static final int FRAME_HEIGHT = 550;
    public static final int GAP_HEIGHT = 80;

    private Matrix mMatrix;

    private int mFrameHeight = FRAME_HEIGHT;
    private int mViewHeight;
    private int mParentHeight;

    public MovableImageView(Context context) {
        super(context);
    }

    public MovableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MovableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        calculateOffset();
        innerSetFrame();

        int saveCount = canvas.save();
        canvas.concat(mMatrix);
        super.onDraw(canvas);
        canvas.restoreToCount(saveCount);
    }

    public void setHeight(int viewHeight, int frameHeight, int parentHeight) {
        mViewHeight = viewHeight;
        mFrameHeight = frameHeight;
        mParentHeight = parentHeight;

        invalidate();
    }

    private void calculateOffset() {
        int posY = 0;
        try {
            posY = (int) ((View) getParent().getParent()).getY();
        } catch (Exception e) {
            MyLog.d(TAG, "calculateOffset e=" + e);
        }

        int gapHeight = mFrameHeight - mViewHeight;
        if (gapHeight > 0) {
            float y = 0f;
            int height = mParentHeight;
            if (height != 0) {
                float ratio = 1f * (posY + mViewHeight / 2) / height;
                if (ratio < 0) {
                    ratio = 0;
                } else if (ratio > 1f) {
                    ratio = 1f;
                }
                y = -ratio * gapHeight;
            }
            setTranslateY(y);
        } else {
            setTranslateY(0);
        }
    }

    public void setOffsetY(float y) {
        setOffsetY(y);
        invalidate();
    }

    protected void setTranslateY(float y) {
        if (mMatrix == null) {
            mMatrix = new Matrix();
        }
        mMatrix.reset();
        mMatrix.setTranslate(0, y);
    }

    public void setFrameHeight(int height) {
        mFrameHeight = height;
    }

    private void innerSetFrame() {
        setFrame(getLeft(), getTop(), getRight(), mFrameHeight);
    }
}
