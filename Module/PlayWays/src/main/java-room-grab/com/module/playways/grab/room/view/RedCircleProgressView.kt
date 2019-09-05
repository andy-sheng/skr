package com.module.playways.grab.room.view


import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.widget.ProgressBar

import com.common.utils.U
import com.common.view.ExPaint
import com.module.playways.R


class RedCircleProgressView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : ProgressBar(context, attrs, defStyle) {

    private val mPaint: Paint = ExPaint()
    private val mRadius = U.getDisplayUtils().dip2px(23.33f)//半径
    private val strokeWidth = U.getDisplayUtils().dip2px(3.0f)//画笔画线的宽度
    private val str: String? = null//时间的字符串

    private var rect: RectF? = null

    private var mRecordAnimator: ValueAnimator? = null

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(resources.getColor(R.color.transparent))//画布背景色

        mPaint.color = resources.getColor(R.color.transparent)
        mPaint.isAntiAlias = true
        mPaint.style = Style.STROKE//空心
        mPaint.strokeWidth = strokeWidth.toFloat()//线的宽度

        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), mRadius.toFloat(), mPaint!!)
        val positions = floatArrayOf(0f, 0.5f, 0f)
        val colors = intArrayOf(Color.RED, Color.GREEN, Color.BLUE)

        val mShader = SweepGradient((width / 2).toFloat(), (height / 2).toFloat(),
                intArrayOf(Color.parseColor("#E9AC1A"), Color.parseColor("#E9AC1A"), Color.parseColor("#E9AC1A"), Color.parseColor("#E9AC1A")), null)

        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.shader = mShader
        mPaint.isDither = true
        mPaint.color = resources.getColor(R.color.white)
        mPaint.strokeWidth = strokeWidth.toFloat()//覆盖线的宽度
        mPaint.isAntiAlias = true
        mPaint.strokeCap = Paint.Cap.ROUND
        val sweepAngle = (max - progress * 1.0f) / max * 360//根据现在值和最大值的百分比计算出弧线现在的度数

        if (rect == null) {
            rect = RectF()
        }
        rect!!.left = ((width - mRadius * 2) / 2).toFloat()
        rect!!.top = ((height - mRadius * 2) / 2).toFloat()

        rect!!.right = rect!!.left + mRadius * 2
        rect!!.bottom = rect!!.top + mRadius * 2

        canvas.drawArc(rect!!, 270f, sweepAngle, false, mPaint!!)

    }

    fun go(duration: Long) {
        cancelAnim()
        max = 360
        mRecordAnimator = ValueAnimator.ofInt(0, 360)
        mRecordAnimator!!.duration = duration
        mRecordAnimator!!.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            progress = value
        }
        mRecordAnimator!!.start()
    }

    private fun cancelAnim() {
        if (mRecordAnimator != null) {
            mRecordAnimator!!.removeAllUpdateListeners()
            mRecordAnimator!!.cancel()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelAnim()
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int,
                           heightMeasureSpec: Int) {
        //根据半径算出占屏幕的比重，圆环宽，padding相关
        val widthSize = mRadius * 2 + strokeWidth * 2 + paddingLeft + paddingRight
        setMeasuredDimension(widthSize, widthSize)
    }

}
