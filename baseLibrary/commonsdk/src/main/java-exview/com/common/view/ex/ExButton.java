package com.common.view.ex;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Button 比TextView 牛逼一些，自带一些点击反馈效果，按下态会有阴影
 * 属性的定义参考这 https://github.com/JavaNoober/BackgroundLibrary
 */
public class ExButton extends android.support.v7.widget.AppCompatButton {

    public ExButton(Context context) {
        super(context);
    }

    public ExButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        loadAttributes(context, attrs);
    }

    public ExButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadAttributes(context, attrs);
    }


    private void loadAttributes(Context context, AttributeSet attrs) {
        AttributeInject.injectBackground(this, context, attrs);
    }

}
