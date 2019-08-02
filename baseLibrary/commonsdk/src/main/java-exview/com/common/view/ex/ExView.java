package com.common.view.ex;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class ExView extends View {
    public ExView(Context context) {
        super(context);
    }

    public ExView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadAttributes(context,attrs);
    }

    public ExView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadAttributes(context,attrs);
    }

    private void loadAttributes(Context context, AttributeSet attrs) {
        AttributeInject.injectBackground(this, context, attrs);
    }

}
