package com.common.view.ex;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class ExRelativeLayout extends RelativeLayout {
    public ExRelativeLayout(Context context) {
        super(context);
    }

    public ExRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadAttributes(context,attrs);
    }

    public ExRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadAttributes(context,attrs);
    }

    private void loadAttributes(Context context, AttributeSet attrs) {
        AttributeInject.injectBackground(this, context, attrs);
    }

}
