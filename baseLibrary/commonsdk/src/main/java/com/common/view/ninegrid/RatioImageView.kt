package com.common.view.ninegrid

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.common.base.R
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.imagebrowse.EnhancedImageView


/**
 * 根据宽高比例自动计算高度ImageView
 * Created by HMY on 2016/4/21.
 */
class RatioImageView : EnhancedImageView {

    /**
     * 宽高比例
     */
    private var mRatio = 0f

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RatioImageView)

        mRatio = typedArray.getFloat(R.styleable.RatioImageView_ratio, 0f)
        typedArray.recycle()
    }

    constructor(context: Context) : super(context) {}

    /**
     * 设置ImageView的宽高比
     *
     * @param ratio
     */
    fun setRatio(ratio: Float) {
        mRatio = ratio
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        val width = MeasureSpec.getSize(widthMeasureSpec)
        if (mRatio != 0f) {
            val height = width / mRatio
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height.toInt(), MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        MyLog.d(TAG, "onLayout changed = $changed, left = $left, top = $top, right = $right, bottom = $bottom")
        /**
         * 首先layout被调用时，立马改变内部view layout，另外覆写addview 方法
         */
        mPhotoDraweeView?.layout(0, 0, width, height)
        mGifImageView?.layout(0, 0, width, height)
        mSubsamplingScaleImageView?.layout(0, 0, width, height)
    }


    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        child?.layout(0, 0, width, height)
        super.addView(child, index, params)
    }

    override fun recycleResWhenDetachedFromWindow() {
        // 覆盖 不让父类方法执行
        mGifFromFile?.stop()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mGifFromFile?.start()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                val drawable = drawable
//                drawable?.mutate()?.setColorFilter(Color.GRAY,
//                        PorterDuff.Mode.MULTIPLY)
//            }
//            MotionEvent.ACTION_MOVE -> {
//            }
//            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
//                val drawableUp = drawable
//                drawableUp?.mutate()?.clearColorFilter()
//            }
//        }

        return super.onTouchEvent(event)
    }

}
