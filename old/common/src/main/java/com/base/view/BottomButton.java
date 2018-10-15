package com.base.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.base.common.R;
import com.base.view.MLTextView;

public class BottomButton extends MLTextView {
    private int mStyle = 0;
    private int mPosition = 0;
    private int mBtnTextStyle = 0;

    public static final int STYLE_NORMAL = 0;
    public static final int STYLE_EMPHASIZE = 1;
    public static final int STYLE_BLACK = 2;
    public static final int STYLE_HIGHLIGHT = 3;
    public static final int STYLE_GREEN = 4;

    public BottomButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BottomButton);
        mStyle = a.getInteger(R.styleable.BottomButton_button_style, 0);
        mPosition = a.getInteger(R.styleable.BottomButton_button_position, 0);

        changeTextColor();
        changeBackground();

        switch (mBtnTextStyle) {
            case 0:
                // 0这个值比较特殊，说明遵循规定的粗体规范：单个及右侧的按钮为粗体，左侧为plain，所以什么都不用做
                break;
            case 1:
                if (!isInEditMode())
                    getPaint().setFakeBoldText(false);
                break;
            case 2:
                if (!isInEditMode())
                    getPaint().setFakeBoldText(true);
                break;
        }

        setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_44));

        a.recycle();
    }

    public void setPosition(int p){
        mPosition = p;
    }

    public BottomButton(Context context) {
        super(context);
        changeTextColor();
        changeBackground();

        switch (mBtnTextStyle) {
            case 0:
                // 0这个值比较特殊，说明遵循规定的粗体规范：单个及右侧的按钮为粗体，左侧为plain，所以什么都不用做
                break;
            case 1:
                if (!isInEditMode())
                    getPaint().setFakeBoldText(false);
                break;
            case 2:
                if (!isInEditMode())
                    getPaint().setFakeBoldText(true);
                break;
        }
        setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_44));
    }

    public void setButtonStyle(int style) {
        mStyle = style;

        changeTextColor();
        changeBackground();
    }

    private void changeTextColor() {
        switch (mStyle) {
            case STYLE_NORMAL:
                setTextColor(getResources().getColor(R.color.color_btn_text_normal));
                break;
            case STYLE_EMPHASIZE:
                setTextColor(getResources().getColor(R.color.color_btn_text_emphasize));
                break;
            case STYLE_BLACK:
                setTextColor(getResources().getColor(R.color.color_btn_text_emphasize_2));
                break;
            case STYLE_HIGHLIGHT:
                setTextColor(getResources().getColor(R.color.color_btn_text_highlight));
                break;
            case STYLE_GREEN:
                setTextColor(getResources().getColor(R.color.color_btn_text_highlight));
                break;
        }
    }

    private void changeBackground() {
        switch (mPosition) {
            case 0:
                setBackgroundResource(R.drawable.bottom_button_single);
//                if (!isInEditMode())
//                    getPaint().setFakeBoldText(true);
                break;
            case 1:
                setBackgroundResource(R.drawable.bottom_button_left);
                break;
            case 2:
                setBackgroundResource(R.drawable.bottom_button_center);
//                if (!isInEditMode())
//                    getPaint().setFakeBoldText(true);
                break;
            case 3:
                setBackgroundResource(R.drawable.bottom_button_right);
//                if (!isInEditMode())
//                    getPaint().setFakeBoldText(true);
                break;
        }

        if (mStyle == STYLE_HIGHLIGHT) {
            setBackgroundResource(R.drawable.bottom_button_highlight);
        } else if (mStyle == STYLE_GREEN) {
            setBackgroundResource(R.drawable.bottom_button_green);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (!isEnabled()) {
            setAlpha(0.5f);
        } else {
            setAlpha(1.0f);
        }
    }
}
