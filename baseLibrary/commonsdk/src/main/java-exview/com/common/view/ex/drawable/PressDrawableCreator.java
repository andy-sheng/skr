package com.common.view.ex.drawable;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;

import com.common.base.R;

public class PressDrawableCreator implements ICreateDrawable {

    private GradientDrawable drawable;
    private TypedArray typedArray;

    PressDrawableCreator(GradientDrawable drawable, TypedArray typedArray) {
        this.drawable = drawable;
        this.typedArray = typedArray;
    }

    @Override
    public Drawable create() throws Exception {
        StateListDrawable stateListDrawable = new StateListDrawable();
        boolean hasSet = false;
        for (int i = 0; i < typedArray.getIndexCount(); i++) {
            int attr = typedArray.getIndex(i);
            if (attr == R.styleable.View_bl_pressed_color) {
                hasSet = true;
                int color = typedArray.getColor(attr, 0);
                GradientDrawable pressDrawable = DrawableFactory.getDrawable(typedArray);
                pressDrawable.setColor(color);
                stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressDrawable);
            } else if (attr == R.styleable.View_bl_unpressed_color) {
                hasSet = true;
                int color = typedArray.getColor(attr, 0);
                drawable.setColor(color);
                stateListDrawable.addState(new int[]{-android.R.attr.state_pressed}, drawable);
            }
        }
        if(!hasSet){
            return null;
        }
        return stateListDrawable;
    }
}
