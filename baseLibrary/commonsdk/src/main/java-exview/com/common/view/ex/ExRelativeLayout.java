package com.common.view.ex;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.common.base.R;
import com.common.view.ex.shadow.ShadowConfig;
import com.common.view.ex.shadow.ShadowHelper;

public class ExRelativeLayout extends RelativeLayout {

    ShadowConfig mShadowConfig;

    public ExRelativeLayout(Context context) {
        super(context);
    }

    public ExRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadAttributes(context, attrs);
    }

    public ExRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadAttributes(context, attrs);
    }

    private void loadAttributes(Context context, AttributeSet attrs) {
        AttributeInject.injectBackground(this, context, attrs);
//        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.background);
//        int shadowColor = typedArray.getInt(R.styleable.background_bl_shadow_Color, Color.TRANSPARENT);
//        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mShadowConfig != null) {
            //实现阴影
            ShadowHelper.draw(canvas, this, mShadowConfig);
        }
    }

    public void setShadowConfig(ShadowConfig shadowConfig) {
        mShadowConfig = shadowConfig;
        //invalidate();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /**
         * 是否需要在顶部把这个事件给拦截住，目前用于点击空白区域 ，美颜面板的消失
         */
        if (mListener != null && mListener.onInterceptTouchEvent(ev)) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    Listener mListener;

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        boolean onInterceptTouchEvent(MotionEvent ev);
    }
}
