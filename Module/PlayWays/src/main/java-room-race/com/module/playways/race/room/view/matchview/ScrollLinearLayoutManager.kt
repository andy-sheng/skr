package com.module.playways.race.room.view.matchview

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.graphics.PointF

class ScrollLinearLayoutManager : LinearLayoutManager {

    // MILLISECONDS_PER_INCH/U.getDisplayUtils().densityDpi 滑动一个pixed需要的时间
    private var MILLISECONDS_PER_INCH = 25f  // 默认值

    constructor(context: Context) : super(context)
    constructor(context: Context, @RecyclerView.Orientation orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun smoothScrollToPosition(recyclerView: RecyclerView?, state: RecyclerView.State?, position: Int) {
        val linearSmoothScroller = object : LinearSmoothScroller(recyclerView?.context) {
            override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
                return this@ScrollLinearLayoutManager
                        .computeScrollVectorForPosition(targetPosition)
            }

            //This returns the milliseconds it takes to
            //scroll one pixel.
            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return MILLISECONDS_PER_INCH / displayMetrics.density
            }
        }
        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }

    //可以用来设置速度
    fun setSpeedSlow(x: Float) {
        MILLISECONDS_PER_INCH = x
    }

}