package com.module.posts.view

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.text.DynamicLayout
import android.text.StaticLayout
import android.util.AttributeSet
import java.lang.reflect.Field

class SpannableTextView : AppCompatTextView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var layout: StaticLayout? = null
        var field: Field? = null
        try {
            val staticField = DynamicLayout::class.java.getDeclaredField("sStaticLayout")
            staticField.setAccessible(true)
            layout = staticField.get(DynamicLayout::class.java) as StaticLayout?
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        if (layout != null) {
            try {
                field = StaticLayout::class.java!!.getDeclaredField("mMaximumVisibleLineCount")
                field?.isAccessible = true
                field?.setInt(layout, maxLines)
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (layout != null && field != null) {
            try {
                field.setInt(layout, Integer.MAX_VALUE)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
    }
}