package com.common.view.ex;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class ExFrameLayout extends FrameLayout {
    public ExFrameLayout(Context context) {
        super(context);
    }

    public ExFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadAttributes(context,attrs);
    }

    public ExFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadAttributes(context,attrs);
    }

    private void loadAttributes(Context context, AttributeSet attrs) {
        AttributeInject.injectBackground(this, context, attrs);
    }

}
