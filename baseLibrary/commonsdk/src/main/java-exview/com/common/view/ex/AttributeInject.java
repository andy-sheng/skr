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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;


import com.common.base.R;
import com.common.view.ex.drawable.DrawableFactory;

public class AttributeInject {

    public static void injectBackground(View view, Context context, AttributeSet attrs) {
        TypedArray bgArray = context.obtainStyledAttributes(attrs, R.styleable.View);
        TypedArray textTa = context.obtainStyledAttributes(attrs, R.styleable.TextView);

        if (bgArray.getIndexCount() == 0 && textTa.getIndexCount() == 0) {
            return;
        }

        try {
            Drawable stateListDrawable = null;
            GradientDrawable drawable = null;

            stateListDrawable = DrawableFactory.getSelectorDrawable(bgArray);
            if (stateListDrawable != null) {
                view.setClickable(true);
                if (view instanceof RadioButton) {
                    ((RadioButton) view).setButtonDrawable(stateListDrawable);
                } else if (view instanceof CheckBox) {
                    ((CheckBox) view).setButtonDrawable(stateListDrawable);
                } else {
                    view.setBackground(stateListDrawable);
                }
            } else {
                drawable = DrawableFactory.getDrawable(bgArray);
                stateListDrawable = DrawableFactory.getPressDrawable(drawable, bgArray);
                if (stateListDrawable != null) {
                    view.setClickable(true);
                    view.setBackground(stateListDrawable);
                } else {
                    view.setBackground(drawable);
                }
            }

            if (view instanceof TextView && textTa.getIndexCount() > 0) {
                ((TextView) view).setTextColor(DrawableFactory.getTextSelectorColor(textTa));
            }

            if (bgArray.getBoolean(R.styleable.View_bl_ripple_enable, false) &&
                    bgArray.hasValue(R.styleable.View_bl_ripple_color)) {
                int color = bgArray.getColor(R.styleable.View_bl_ripple_color, 0);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Drawable contentDrawable = (stateListDrawable == null ? drawable : stateListDrawable);
                    RippleDrawable rippleDrawable = new RippleDrawable(ColorStateList.valueOf(color), contentDrawable, contentDrawable);
                    view.setClickable(true);
                    view.setBackground(rippleDrawable);
                } else {
                    StateListDrawable tmpDrawable = new StateListDrawable();
                    GradientDrawable unPressDrawable = DrawableFactory.getDrawable(bgArray);
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
            bgArray.recycle();
            textTa.recycle();
        }

        return;
    }

    public static void injectSrc(ImageView view, Context context, AttributeSet attrs) {
        TypedArray srcSelector = context.obtainStyledAttributes(attrs, R.styleable.ImageView);

        if (srcSelector.getIndexCount() == 0) {
            return;
        }

        try {
            StateListDrawable stateListDrawable = null;
            if (srcSelector.getIndexCount() > 0) {
                stateListDrawable = DrawableFactory.getSrcSelectorDrawable(srcSelector);
                view.setClickable(true);
                view.setImageDrawable(stateListDrawable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            srcSelector.recycle();
        }
    }

}
