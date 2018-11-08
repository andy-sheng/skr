package com.common.view.ex;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;


import com.common.base.R;
import com.common.view.ex.drawable.DrawableFactory;

public class BackgroundInject {

    public static void inject(View view, Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.background);
        TypedArray pressTa = context.obtainStyledAttributes(attrs, R.styleable.background_press);
        TypedArray selectorTa = context.obtainStyledAttributes(attrs, R.styleable.background_selector);
        TypedArray textTa = context.obtainStyledAttributes(attrs, R.styleable.text_selector);
        if (typedArray.getIndexCount() == 0 && selectorTa.getIndexCount() == 0
                && pressTa.getIndexCount() == 0 && textTa.getIndexCount() == 0) {
            return;
        }

        try {
            GradientDrawable drawable = null;
            StateListDrawable stateListDrawable = null;
            if (selectorTa.getIndexCount() > 0) {
                stateListDrawable = DrawableFactory.getSelectorDrawable(typedArray, selectorTa);
                view.setClickable(true);
                if (view instanceof RadioButton) {
                    ((RadioButton) view).setButtonDrawable(stateListDrawable);
                } else if (view instanceof CheckBox) {
                    ((CheckBox) view).setButtonDrawable(stateListDrawable);
                } else {
                    view.setBackground(stateListDrawable);
                }
            } else if (pressTa.getIndexCount() > 0) {
                drawable = DrawableFactory.getDrawable(typedArray);
                stateListDrawable = DrawableFactory.getPressDrawable(drawable, typedArray, pressTa);
                view.setClickable(true);
                view.setBackground(stateListDrawable);
            } else {
                drawable = DrawableFactory.getDrawable(typedArray);
                view.setBackground(drawable);
            }

            if (view instanceof TextView && textTa.getIndexCount() > 0) {
                ((TextView) view).setTextColor(DrawableFactory.getTextSelectorColor(textTa));
            }

            if (typedArray.getBoolean(R.styleable.background_bl_ripple_enable, false) &&
                    typedArray.hasValue(R.styleable.background_bl_ripple_color)) {
                int color = typedArray.getColor(R.styleable.background_bl_ripple_color, 0);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Drawable contentDrawable = (stateListDrawable == null ? drawable : stateListDrawable);
                    RippleDrawable rippleDrawable = new RippleDrawable(ColorStateList.valueOf(color), contentDrawable, contentDrawable);
                    view.setClickable(true);
                    view.setBackground(rippleDrawable);
                } else {
                    StateListDrawable tmpDrawable = new StateListDrawable();
                    GradientDrawable unPressDrawable = DrawableFactory.getDrawable(typedArray);
                    unPressDrawable.setColor(color);
                    tmpDrawable.addState(new int[]{-android.R.attr.state_pressed}, drawable);
                    tmpDrawable.addState(new int[]{android.R.attr.state_pressed}, unPressDrawable);
                    view.setClickable(true);
                    view.setBackground(tmpDrawable);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            typedArray.recycle();
            pressTa.recycle();
            selectorTa.recycle();
            textTa.recycle();
        }

        return;
    }

}
