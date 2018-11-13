package com.common.view.ex.drawable;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.AttrRes;

import com.common.base.R;

/**
 * Created by xzy on 18-11-10.
 */

public class SrcSlecterDrawableCreater implements ICreateDrawable {

    private TypedArray selectorTa;

    public SrcSlecterDrawableCreater(TypedArray selectorTa) {
        this.selectorTa = selectorTa;
    }

    @Override
    public Drawable create() throws Exception {
        StateListDrawable stateListDrawable = new StateListDrawable();

        for (int i = 0; i < selectorTa.getIndexCount(); i++) {
            int attr = selectorTa.getIndex(i);

            if (attr == R.styleable.src_selector_src_checkable_drawable) {
                setSelectorDrawable(selectorTa, stateListDrawable, attr, android.R.attr.state_checkable);
            } else if (attr == R.styleable.src_selector_src_unCheckable_drawable) {
                setSelectorDrawable(selectorTa, stateListDrawable, attr, -android.R.attr.state_checkable);
            } else if (attr == R.styleable.src_selector_src_checked_drawable) {
                setSelectorDrawable(selectorTa, stateListDrawable, attr, android.R.attr.state_checked);
            } else if (attr == R.styleable.src_selector_src_unChecked_drawable) {
                setSelectorDrawable(selectorTa, stateListDrawable, attr, -android.R.attr.state_checked);
            } else if (attr == R.styleable.src_selector_src_enabled_drawable) {
                setSelectorDrawable(selectorTa, stateListDrawable, attr, android.R.attr.state_enabled);
            } else if (attr == R.styleable.src_selector_src_unEnabled_drawable) {
                setSelectorDrawable(selectorTa, stateListDrawable, attr, -android.R.attr.state_enabled);
            } else if (attr == R.styleable.src_selector_src_selected_drawable) {
                setSelectorDrawable(selectorTa, stateListDrawable, attr, android.R.attr.state_selected);
            } else if (attr == R.styleable.src_selector_src_unSelected_drawable) {
                setSelectorDrawable(selectorTa, stateListDrawable, attr, -android.R.attr.state_selected);
            } else if (attr == R.styleable.src_selector_src_pressed_drawable) {
                setSelectorDrawable(selectorTa, stateListDrawable, attr, android.R.attr.state_pressed);
            } else if (attr == R.styleable.src_selector_src_unPressed_drawable) {
                setSelectorDrawable(selectorTa, stateListDrawable, attr, -android.R.attr.state_pressed);
            } else if (attr == R.styleable.src_selector_src_focused_drawable) {
                setSelectorDrawable(selectorTa, stateListDrawable, attr, android.R.attr.state_focused);
            } else if (attr == R.styleable.src_selector_src_unFocused_drawable) {
                setSelectorDrawable(selectorTa, stateListDrawable, attr, -android.R.attr.state_focused);
            } else if (attr == R.styleable.src_selector_src_focused_hovered) {
                setSelectorDrawable(selectorTa, stateListDrawable, attr, android.R.attr.state_hovered);
            } else if (attr == R.styleable.src_selector_src_unFocused_hovered) {
                setSelectorDrawable(selectorTa, stateListDrawable, attr, -android.R.attr.state_hovered);
            } else if (attr == R.styleable.src_selector_src_focused_activated) {
                setSelectorDrawable(selectorTa, stateListDrawable, attr, android.R.attr.state_activated);
            } else if (attr == R.styleable.src_selector_src_unFocused_activated) {
                setSelectorDrawable(selectorTa, stateListDrawable, attr, -android.R.attr.state_activated);
            }
        }
        return stateListDrawable;
    }

    private void setSelectorDrawable(TypedArray selectorTa,
                                     StateListDrawable stateListDrawable, int attr, @AttrRes int functionId) throws Exception {
        //这里用try catch先判断是否是颜色而不是直接调用getDrawable，为了方便填入的是颜色时可以沿用其他属性,
        //否则如果是其他资源会覆盖app:corners_radius等其他shape属性设置的效果
        Drawable resDrawable = selectorTa.getDrawable(attr);
        stateListDrawable.addState(new int[]{functionId}, resDrawable);
    }
}
