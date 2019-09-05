package com.module.playways.grab.room.view

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View

import com.common.utils.U
import com.common.view.ExPaint

/**
 * 从坐上角开始
 */
class RoundRectangleView : View {
    val TAG = "RoundRectangleView"
    private val max = 100
    private val min = 0

    //右上角开始,满足 0 < startAngle < 90
    private val startAngle = 0

    //右下角结束,满足 270 < startAngle < 360
    private val endAngle = 360

    //毫秒
    private var mDuration: Long = 3000

    private var currentProgress = 3000

    private val mPaint: Paint = ExPaint()

    private var mRadio: Int = 0

    private var mLineWidth: Int = 0

    private val circleCenterA = RectF()

    private val circleCenterB = RectF()

    private var totalLenght = 0

    private var mAnimatorSet: AnimatorSet? = null

    private var mProgressWidth = U.getDisplayUtils().dip2px(3f)

    private val measuredWidthR: Int
        get() = measuredWidth - mProgressWidth

    private val measuredHeightR: Int
        get() = measuredHeight - mProgressWidth

    private var hasData = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //MyLog.d(TAG, "width " + getMeasuredWidth() + " height " + getMeasuredHeight());
        mRadio = measuredHeightR / 2

        mLineWidth = measuredWidthR - measuredHeight

        totalLenght = (2.0 * Math.PI * mRadio.toDouble()).toInt() + mLineWidth * 2

        circleCenterA.set(
                (mProgressWidth / 2).toFloat(), (mProgressWidth / 2).toFloat(),
                measuredHeightR.toFloat(),
                measuredHeightR.toFloat()
        )

        circleCenterB.set(
                (measuredWidthR - measuredHeightR).toFloat(), (mProgressWidth / 2).toFloat(),
                measuredWidthR.toFloat(),
                measuredHeightR.toFloat()
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!hasData) {
            return
        }
        //分五个区域画，从最后一个部分开始画
        var a = endAngle - 270
        var arc = 2.0 * Math.PI * mRadio.toDouble() * a.toDouble() / 360

        var angle = percentageToAngle(currentProgress, a)

        canvas.drawArc(circleCenterB, 0f,
                angle.toFloat(), false, mPaint)

        var lastedProgress = currentProgress - arc.toInt()

        if (lastedProgress <= 0) {
            return
        }

        val bottomLineLengh = progressToLine(lastedProgress, mLineWidth)

        canvas.drawLine((measuredWidth - measuredHeight / 2).toFloat(), (measuredHeight - mProgressWidth).toFloat(), (mRadio + (mLineWidth - bottomLineLengh)).toFloat(), (measuredHeight - mProgressWidth).toFloat(), mPaint)

        lastedProgress -= bottomLineLengh
        if (lastedProgress <= 0) {
            return
        }

        arc = 2.0 * Math.PI * mRadio.toDouble() * 180.0 / 360
        angle = percentageToAngle(lastedProgress, 180)
        canvas.drawArc(circleCenterA, (0 + 90).toFloat(),
                angle.toFloat(), false, mPaint)

        lastedProgress -= arc.toInt()
        if (lastedProgress <= 0) {
            return
        }

        val topLineLengh = progressToLine(lastedProgress, mLineWidth)
        canvas.drawLine((measuredHeight / 2).toFloat(), (mProgressWidth / 2).toFloat(), (measuredHeight / 2 + topLineLengh).toFloat(), (mProgressWidth / 2).toFloat(), mPaint)

        lastedProgress -= mLineWidth
        if (lastedProgress <= 0) {
            return
        }

        a = 90 - startAngle
        angle = percentageToAngle(lastedProgress, a)
        canvas.drawArc(circleCenterB, (0 - 90).toFloat(),
                angle.toFloat(), false, mPaint)

    }

    fun init() {
        mPaint.isAntiAlias = true
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeWidth = mProgressWidth.toFloat()
        mPaint.style = Paint.Style.STROKE
        mPaint.color = Color.parseColor("#FFFFFF")
        //        mPaint.setShader(generateOutSweepGradient());
    }

    private fun generateOutSweepGradient(): SweepGradient {
        val sweepGradient = SweepGradient(mRadio.toFloat(), mRadio.toFloat(),
                intArrayOf(-0x129f, -0x129f),
                floatArrayOf(0f, 0f)
        )

        val matrix = Matrix()
        //        matrix.setRotate(mStartAngle - 10, mCenterX, mCenterY);
        sweepGradient.setLocalMatrix(matrix)

        return sweepGradient
    }

    fun startCountDown(duration: Long) {
        if (duration <= 0) {
            return
        }

        hasData = true

        mDuration = duration

        val creditValueAnimator = ValueAnimator.ofInt(totalLenght, 0)
        creditValueAnimator.duration = mDuration
        creditValueAnimator.addUpdateListener { animation ->
            currentProgress = animation.animatedValue as Int
            //                MyLog.d(TAG, "currentProgress " + currentProgress);
            postInvalidate()
        }

        mAnimatorSet = AnimatorSet()
        mAnimatorSet!!.playTogether(creditValueAnimator)
        mAnimatorSet!!.start()
    }

    fun stopCountDown() {
        hasData = false
        if (mAnimatorSet != null) {
            mAnimatorSet!!.removeAllListeners()
            mAnimatorSet!!.cancel()
        }
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mAnimatorSet != null) {
            mAnimatorSet!!.removeAllListeners()
            mAnimatorSet!!.cancel()
        }
    }

    /**
     * 百分比变成角度
     */
    private fun percentageToAngle(current: Int, need: Int): Int {
        val arc = 2.0 * Math.PI * mRadio.toDouble() * need.toDouble() / 360
        return if (current >= arc) {
            need
        } else {
            val realAngle = current.toDouble() / (2.0 * Math.PI * mRadio.toDouble() / 360)
            realAngle.toInt()
        }
    }

    /**
     * 百分比变成角度
     */
    private fun progressToLine(current: Int, need: Int): Int {
        return if (current >= need) {
            need
        } else {
            current
        }
    }
}
