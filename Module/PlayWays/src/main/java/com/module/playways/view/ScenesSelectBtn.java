package com.module.playways.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;

import com.module.playways.R;

public class ScenesSelectBtn extends android.support.v7.widget.AppCompatRadioButton {

    public ScenesSelectBtn(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ScenesSelectBtn(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.ScenesSelectBtn);

        int strokeColor = array.getColor(R.styleable.ScenesSelectBtn_strokeColor, Color.parseColor("#ffffff"));
        int fillColor = array.getColor(R.styleable.ScenesSelectBtn_fillColor, Color.parseColor("#ffffff"));

        int strokeWidth = 5; // 3px not dp
        int roundRadius = 15; // 8px not dp

        GradientDrawable uncheckDrawable = new GradientDrawable();
        uncheckDrawable.setShape(GradientDrawable.OVAL);
        uncheckDrawable.setColor(fillColor);
        uncheckDrawable.setCornerRadius(roundRadius);
        uncheckDrawable.setStroke(strokeWidth, strokeColor);

        strokeColor = array.getColor(R.styleable.ScenesSelectBtn_strokeColor_checked, strokeColor);
        fillColor = array.getColor(R.styleable.ScenesSelectBtn_fillColor_checked, fillColor);

        GradientDrawable checkDrawable = new GradientDrawable();
        checkDrawable.setShape(GradientDrawable.OVAL);
        checkDrawable.setColor(fillColor);
        checkDrawable.setCornerRadius(roundRadius);
        checkDrawable.setStroke(strokeWidth, strokeColor);


        StateListDrawable drawable = new StateListDrawable();
        //Non focused states
        drawable.addState(new int[]{-android.R.attr.state_checked},
                uncheckDrawable);

        drawable.addState(new int[]{android.R.attr.state_checked},
                checkDrawable);

        setBackground(drawable);

        int textColorNormal = array.getColor(R.styleable.ScenesSelectBtn_textColor_normal, Color.parseColor("#60ffffff"));
        int textColorChecked = array.getColor(R.styleable.ScenesSelectBtn_textColor_checked, Color.parseColor("#ffffff"));

        int[] colors = new int[]{textColorNormal, textColorChecked,textColorNormal};
        int[][] states = new int[3][];
        states[0] = new int[]{-android.R.attr.state_checked};
        states[1] = new int[]{android.R.attr.state_checked};
        states[2] = new int[]{};
        ColorStateList colorStateList = new ColorStateList(states, colors);
        setTextColor(colorStateList);

    }
}
