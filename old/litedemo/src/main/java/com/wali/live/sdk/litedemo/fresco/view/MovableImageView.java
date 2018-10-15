package com.wali.live.sdk.litedemo.fresco.view;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Created by lan on 2016/9/25.
 */
public class MovableImageView extends SimpleDraweeView {
    private int mFrameHeight = 650;

    private Matrix mMatrix;

    public MovableImageView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
    }

    public MovableImageView(Context context) {
        super(context);
    }

    public MovableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MovableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MovableImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        innerSetFrame();

        int saveCount = canvas.save();
        canvas.concat(mMatrix);
        super.onDraw(canvas);
        canvas.restoreToCount(saveCount);
    }

    public void setOffsetY(float y) {
        if (mMatrix == null) {
            mMatrix = new Matrix();
        }
        mMatrix.reset();
        mMatrix.setTranslate(0, y);
        invalidate();
    }

    public void setFrameHeight(int height) {
        mFrameHeight = height;
    }

    private void innerSetFrame() {
        setFrame(getLeft(), getTop(), getRight(), mFrameHeight);
    }
}
