package com.module.rankingmode.prepare.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;

import com.module.rankingmode.R;

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

        int strokeWidth = 5; // 3px not dp
        int roundRadius = 15; // 8px not dp


        int strokeColor = array.getColor(R.styleable.ScenesSelectBtn_strokeColor, Color.parseColor("#ffffff"));
        int fillColor = array.getColor(R.styleable.ScenesSelectBtn_fillColor, Color.parseColor("#ffffff"));

        GradientDrawable uncheckDrawable = new GradientDrawable();
        uncheckDrawable.setShape(GradientDrawable.OVAL);
        uncheckDrawable.setColor(fillColor);
        uncheckDrawable.setCornerRadius(roundRadius);
        uncheckDrawable.setStroke(strokeWidth, strokeColor);

        strokeColor = Color.parseColor("#00ffff");
        fillColor = Color.parseColor("#ff00ff");

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

    }
}
