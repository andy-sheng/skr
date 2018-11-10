package com.common.view.ex;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * AppCompatImageView 自适应字体大小
 * 这段文档中最后一段比较重要，Android官方提示开发者，如果开发者在xml布局中写了一个过去传统的Android的TextView，
 * 那么这个TextView会被自动被Android编译系统替换成AppCompatTextView。
 * 在在Android O（8.0）系统及以上，TextView和AppCompatTextView是相同的。
 * 在低于8.0的版本上，开发者可在自定义和布局中需要写文本View时候，可以使用AppCompatTextView，
 * 以使用到Android最新的自适应大小的特性。
 *
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
        BackgroundInject.injectBackground(this, context, attrs);
        BackgroundInject.injectSrc(this, context, attrs);
    }

}
