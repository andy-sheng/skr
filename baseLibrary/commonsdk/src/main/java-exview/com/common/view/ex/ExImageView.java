package com.common.view.ex;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 属性的定义参考这 https://github.com/JavaNoober/BackgroundLibrary
 */
public class ExImageView extends android.support.v7.widget.AppCompatImageView {

    public ExImageView(Context context) {
        super(context);
    }

    public ExImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        loadAttributes(context, attrs);
    }

    public ExImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadAttributes(context, attrs);
    }


    private void loadAttributes(Context context, AttributeSet attrs) {
        BackgroundInject.inject(this, context, attrs);
    }

}
