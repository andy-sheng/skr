package com.common.view.ex.drawable;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;

import org.xmlpull.v1.XmlPullParserException;

/**
 * Created by xiaoqi on 2018/9/12
 */
public class DrawableFactory {

    //获取shape属性的drawable
    public static GradientDrawable getDrawable(TypedArray typedArray) throws XmlPullParserException {
        return (GradientDrawable) new GradientDrawableCreator(typedArray).create();
    }

    //获取selector属性的drawable
    public static StateListDrawable getSelectorDrawable(TypedArray typedArray, TypedArray selectorTa) throws Exception {
        return (StateListDrawable) new SelectorDrawableCreator(typedArray, selectorTa).create();
    }

    //获取SRC selector属性的drawable
    public static StateListDrawable getSrcSelectorDrawable(TypedArray selectorTa) throws Exception {
        return (StateListDrawable) new SrcSlecterDrawableCreater(selectorTa).create();
    }

    //获取selector属性关于text的color
    public static ColorStateList getTextSelectorColor(TypedArray textTa) {
        return new ColorStateCreator(textTa).create();
    }

    //适配早期版本的属性
    public static StateListDrawable getPressDrawable(GradientDrawable drawable, TypedArray typedArray, TypedArray pressTa)
            throws Exception {
        return (StateListDrawable) new PressDrawableCreator(drawable, typedArray, pressTa).create();
    }

}
