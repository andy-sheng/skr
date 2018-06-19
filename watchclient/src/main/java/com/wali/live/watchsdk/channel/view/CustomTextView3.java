package com.wali.live.watchsdk.channel.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.base.log.MyLog;

/**
 * Created by liuyanyan on 2018/1/23.
 *
 * @module 友乐
 */

public class CustomTextView3 extends android.support.v7.widget.AppCompatTextView {

    public CustomTextView3(Context context) {
        super(context);
        setTextStyle();
    }

    public CustomTextView3(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTextStyle();
    }

    public CustomTextView3(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTextStyle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            //Todo:该字体在显示对时候，右上角会被切掉一部分，显示不全,所以多加5个pix
            int width = getMeasuredWidth();
            int height = getMeasuredHeight();
            setMeasuredDimension(width + 5, height);
        } catch (ArrayIndexOutOfBoundsException e) {
            MyLog.e(e);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            setMeasuredDimension(widthSize, heightSize);
        } catch (IndexOutOfBoundsException e) {
            MyLog.e(e);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            setMeasuredDimension(widthSize, heightSize);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (ArrayIndexOutOfBoundsException e) {
            MyLog.e(e);
        } catch (IndexOutOfBoundsException e) {
            MyLog.e(e);
        }
    }

    private void setTextStyle() {
        setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-MediumItalic.ttf"));
    }
}
