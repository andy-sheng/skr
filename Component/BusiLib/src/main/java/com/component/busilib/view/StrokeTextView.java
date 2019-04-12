package com.component.busilib.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.common.utils.U;
import com.component.busilib.R;

import java.lang.reflect.Field;

public class StrokeTextView extends TextView {

    private TextPaint textPaint;

    private int borderWidth = 3;  //px
    private int textColor = Color.WHITE;
    private int borderColor = Color.BLACK;

    public StrokeTextView(Context context) {
        super(context);
        init(context, null);
    }

    public StrokeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public StrokeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.StrokeTextView);
        textColor = typedArray.getColor(R.styleable.StrokeTextView_textColor, 0);
        borderColor = typedArray.getColor(R.styleable.StrokeTextView_borderColor, 0);
        borderWidth = typedArray.getDimensionPixelOffset(R.styleable.StrokeTextView_borderWidth, 0);
        typedArray.recycle();

        textPaint = getPaint();
    }

//    public void setStroke(boolean stroke) {
//        isStroke = stroke;
//        invalidate();
//    }
//
//    public void setBorderWidth(int borderWidth) {
//        this.borderWidth = borderWidth;
//        invalidate();
//    }
//
//    public void setBorderColor(@ColorInt int borderColor) {
//        this.borderColor = borderColor;
//        invalidate();
//    }

    @Override
    public void setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
        super.setTextColor(textColor);
    }

    @Override
    public void setTextColor(ColorStateList colors) {
        this.textColor = colors.getColorForState(getDrawableState(), textColor);
        super.setTextColor(colors);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (textColor == borderColor) {
            borderColor = U.getColorUtils().toDarkenColor(borderColor, 0.7f);
        }

        // 描外层
        setTextColorUseReflection(borderColor);
        textPaint.setStrokeWidth(borderWidth);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setFakeBoldText(true); // 外层文字采用粗体
        super.onDraw(canvas);

        // 描内层
        setTextColorUseReflection(textColor);
        textPaint.setStrokeWidth(0);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setFakeBoldText(true); // 内层文字不采用粗体
        super.onDraw(canvas);
    }

    /**
     * 使用反射的方法进行字体颜色的设置
     */
    private void setTextColorUseReflection(@ColorInt int color) {
        //noinspection TryWithIdenticalCatches
        try {
            Field field = TextView.class.getDeclaredField("mCurTextColor");
            field.setAccessible(true);
            field.set(this, color);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        textPaint.setColor(color);
    }
}
