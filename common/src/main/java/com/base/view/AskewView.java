package com.base.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.base.common.R;

/**
 * Created by lan on 2016/9/11.
 *
 * @module 自定义控件
 * @description 目前只支持-45度角
 */
public class AskewView extends TextView {
    protected static final String TAG = AskewView.class.getSimpleName();

    private int mWidth;
    private int mHeight;

    private int mAskewAgree = -45;
    private int mAdjustDistance;

    public AskewView(Context context) {
        super(context);
    }

    public AskewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttrs(context, attrs);
    }

    public AskewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAttrs(context, attrs);
    }

    private void setAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AskewView);
        mAdjustDistance = a.getDimensionPixelSize(R.styleable.AskewView_adjustDistance, 0);
        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mWidth = getWidth();
        mHeight = getHeight();

        setRotation(mAskewAgree);

        int deltaX = (mHeight - mWidth) / 4;
        int deltaY = -(mHeight - mWidth) / 4;
        setTranslationX(deltaX + mAdjustDistance);
        setTranslationY(deltaY + mAdjustDistance);
    }
}
