package com.module.playways.grab.room.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

import com.common.view.ex.ExRelativeLayout

import java.util.ArrayList

class GrabRootView : ExRelativeLayout {

    private var mOnTouchListeners: MutableList<View.OnTouchListener> = ArrayList()

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        setOnTouchListener(OnTouchListener { v, event ->
            for (l in mOnTouchListeners) {
                val r = l.onTouch(v, event)
                if (r) {
                    return@OnTouchListener true
                }
            }
            false
        })
    }

    fun addOnTouchListener(l: View.OnTouchListener) {
        if (mOnTouchListeners.contains(l)) {
            mOnTouchListeners.remove(l)
        }
        mOnTouchListeners.add(l)
    }

    fun removeOnTouchListener(l: View.OnTouchListener) {
        mOnTouchListeners.remove(l)
    }
}
