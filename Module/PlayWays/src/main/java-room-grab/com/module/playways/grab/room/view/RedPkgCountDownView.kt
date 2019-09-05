package com.module.playways.grab.room.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout

import com.module.playways.R

class RedPkgCountDownView : RelativeLayout {
    private val mRedCircleCountDownView: RedCircleProgressView

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.red_pkg_count_down_view_layout, this)
        mRedCircleCountDownView = findViewById(R.id.red_circle_count_down_view)
    }

    fun startCountDown(duration: Long) {
        mRedCircleCountDownView.go(duration)
    }
}
