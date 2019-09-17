package com.common.view.ex;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.common.log.MyLog;
import com.common.view.ex.stv.SuperTextView;

import static com.tencent.smtt.sdk.TbsReaderView.TAG;


/**
 * AppCompatTextView 自适应字体大小
 * 这段文档中最后一段比较重要，Android官方提示开发者，如果开发者在xml布局中写了一个过去传统的Android的TextView，
 * 那么这个TextView会被自动被Android编译系统替换成AppCompatTextView。
 * 在在Android O（8.0）系统及以上，TextView和AppCompatTextView是相同的。
 * 在低于8.0的版本上，开发者可在自定义和布局中需要写文本View时候，可以使用AppCompatTextView，
 * 以使用到Android最新的自适应大小的特性。
 *
 * 属性的定义参考这 https://github.com/JavaNoober/BackgroundLibrary
 */
public class ExTextView extends SuperTextView{

    public ExTextView(Context context) {
        super(context);
    }

    public ExTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        loadAttributes(context, attrs);
    }

    public ExTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadAttributes(context, attrs);
    }

    private void loadAttributes(Context context, AttributeSet attrs) {
        AttributeInject.injectBackground(this, context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //MyLog.d("ExTextView","onLayout" + " changed=" + changed + " left=" + left + " top=" + top + " right=" + right + " bottom=" + bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //MyLog.d("ExTextView","onDraw" + " canvas=" + canvas);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //MyLog.d("ExTextView","onSizeChanged" + " w=" + w + " h=" + h + " oldw=" + oldw + " oldh=" + oldh);
    }

//    @Override
//    public void invalidateDrawable(Drawable drawable) {
//        invalidate();
//        MyLog.d("ExTextView","invalidateDrawable" + " drawable=" + drawable);
//    }

}
