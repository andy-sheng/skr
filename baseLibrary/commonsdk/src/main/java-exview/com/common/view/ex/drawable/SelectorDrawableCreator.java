package com.common.view.ex.drawable;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.AttrRes;

import com.common.base.R;


public class SelectorDrawableCreator implements ICreateDrawable {

    private TypedArray bgArray;

    boolean hasSet = false;

    public SelectorDrawableCreator(TypedArray typedArray) {
        this.bgArray = typedArray;
    }

    @Override
    public Drawable create() throws Exception {
        StateListDrawable stateListDrawable = new StateListDrawable();

        for (int i = 0; i < bgArray.getIndexCount(); i++) {
            int attr = bgArray.getIndex(i);
            if (attr == R.styleable.View_bl_checkable_drawable) {
                setSelectorDrawable(bgArray, stateListDrawable, attr, android.R.attr.state_checkable);
            } else if (attr == R.styleable.View_bl_unCheckable_drawable) {
                setSelectorDrawable(bgArray, stateListDrawable, attr, -android.R.attr.state_checkable);
            } else if (attr == R.styleable.View_bl_checked_drawable) {
                setSelectorDrawable(bgArray, stateListDrawable, attr, android.R.attr.state_checked);
            } else if (attr == R.styleable.View_bl_unChecked_drawable) {
                setSelectorDrawable(bgArray, stateListDrawable, attr, -android.R.attr.state_checked);
            } else if (attr == R.styleable.View_bl_enabled_drawable) {
                setSelectorDrawable(bgArray, stateListDrawable, attr, android.R.attr.state_enabled);
            } else if (attr == R.styleable.View_bl_unEnabled_drawable) {
                setSelectorDrawable(bgArray, stateListDrawable, attr, -android.R.attr.state_enabled);
            } else if (attr == R.styleable.View_bl_selected_drawable) {
                setSelectorDrawable(bgArray, stateListDrawable, attr, android.R.attr.state_selected);
            } else if (attr == R.styleable.View_bl_unSelected_drawable) {
                setSelectorDrawable(bgArray, stateListDrawable, attr, -android.R.attr.state_selected);
            } else if (attr == R.styleable.View_bl_pressed_drawable) {
                setSelectorDrawable(bgArray, stateListDrawable, attr, android.R.attr.state_pressed);
            } else if (attr == R.styleable.View_bl_unPressed_drawable) {
                setSelectorDrawable(bgArray, stateListDrawable, attr, -android.R.attr.state_pressed);
            } else if (attr == R.styleable.View_bl_focused_drawable) {
                setSelectorDrawable(bgArray, stateListDrawable, attr, android.R.attr.state_focused);
            } else if (attr == R.styleable.View_bl_unFocused_drawable) {
                setSelectorDrawable(bgArray, stateListDrawable, attr, -android.R.attr.state_focused);
            } else if (attr == R.styleable.View_bl_focused_hovered) {
                setSelectorDrawable(bgArray, stateListDrawable, attr, android.R.attr.state_hovered);
            } else if (attr == R.styleable.View_bl_unFocused_hovered) {
                setSelectorDrawable(bgArray, stateListDrawable, attr, -android.R.attr.state_hovered);
            } else if (attr == R.styleable.View_bl_focused_activated) {
                setSelectorDrawable(bgArray, stateListDrawable, attr, android.R.attr.state_activated);
            } else if (attr == R.styleable.View_bl_unFocused_activated) {
                setSelectorDrawable(bgArray, stateListDrawable, attr, -android.R.attr.state_activated);
            }
        }
        if (!hasSet) {
            return null;
        }
        return stateListDrawable;
    }

    private void setSelectorDrawable(TypedArray typedArray,
                                     StateListDrawable stateListDrawable, int attr, @AttrRes int functionId) throws Exception {
        hasSet = true;
        int color = 0;
        Drawable resDrawable = null;

        //这里用try catch先判断是否是颜色而不是直接调用getDrawable，为了方便填入的是颜色时可以沿用其他属性,
        //否则如果是其他资源会覆盖app:corners_radius等其他shape属性设置的效果
        try {
            color = typedArray.getColor(attr, 0);
            if (color == 0) {
                resDrawable = typedArray.getDrawable(attr);
            }
        } catch (Exception e) {
            resDrawable = typedArray.getDrawable(attr);
        }
        if (resDrawable == null && color != 0) {
            GradientDrawable tmpDrawable = (GradientDrawable) new GradientDrawableCreator(typedArray).create();
            tmpDrawable.setColor(color);
            stateListDrawable.addState(new int[]{functionId}, tmpDrawable);
        } else {
            stateListDrawable.addState(new int[]{functionId}, resDrawable);
        }
    }
}
