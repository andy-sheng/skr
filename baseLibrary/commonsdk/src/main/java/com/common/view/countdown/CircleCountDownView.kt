package com.common.view.countdown

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar

import com.common.base.R
import com.common.utils.U

class CircleCountDownView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : ProgressBar(context, attrs, defStyle) {

    private var mPaint: Paint? = null
    private var mBgPaint: Paint? = null
    private var mRadius = U.getDisplayUtils().dip2px(23.33f).toFloat()//半径
    private var strokeWidth = U.getDisplayUtils().dip2px(3.0f).toFloat()//画笔画线的宽度
    private var mDegree = 270// 从那个角度开始转
    private var mBgStrokeColor = U.getColor(R.color.transparent)
    private var mBgColor = U.getColor(R.color.transparent)
    private var mBgStrokeWidth = strokeWidth//画笔画线的宽度
    internal var mColor = Color.parseColor("#E9AC1A")

    private var rect: RectF? = null

    internal var mRecordAnimator: ValueAnimator? = null

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleCountDownView)
            mRadius = typedArray.getDimension(R.styleable.CircleCountDownView_inner_stroke_raduis, 0f)
            strokeWidth = typedArray.getDimension(R.styleable.CircleCountDownView_inner_stroke_width, 0f)
            mColor = typedArray.getColor(R.styleable.CircleCountDownView_inner_color, Color.parseColor("#E9AC1A"))
            mDegree = typedArray.getInteger(R.styleable.CircleCountDownView_degree, 270)
            mBgStrokeColor = typedArray.getColor(R.styleable.CircleCountDownView_stroke_bg_color, U.getColor(R.color.transparent))
            mBgColor = typedArray.getColor(R.styleable.CircleCountDownView_bg_color, U.getColor(R.color.transparent))
            mBgStrokeWidth = typedArray.getDimension(R.styleable.CircleCountDownView_stroke_bg_width, strokeWidth)
            typedArray.recycle()
        }

        mBgPaint = com.common.view.ExPaint()
        val mBgShader = SweepGradient((width / 2).toFloat(), (height / 2).toFloat(),
                intArrayOf(mBgColor, mBgColor, mBgColor, mBgColor), null)
        mBgPaint!!.shader = mBgShader
        mBgPaint!!.isAntiAlias = true
        mBgPaint!!.style = Style.FILL//实心
        mBgPaint!!.strokeWidth = strokeWidth//线的宽度

        mPaint = com.common.view.ExPaint()
        mPaint!!.style = Style.STROKE//空心
        mPaint!!.strokeJoin = Paint.Join.ROUND
        mPaint!!.isDither = true
        mPaint!!.color = resources.getColor(R.color.white)
        mPaint!!.isAntiAlias = true
        mPaint!!.strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(resources.getColor(R.color.transparent))
        //画整个背景
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), (width / 2).toFloat(), mBgPaint!!)

        //画进度条的背景
        val mBgStrokeShader = SweepGradient((width / 2).toFloat(), (height / 2).toFloat(),
                intArrayOf(mBgStrokeColor, mBgStrokeColor, mBgStrokeColor, mBgStrokeColor), null)

        mPaint!!.shader = mBgStrokeShader
        mPaint!!.strokeWidth = mBgStrokeWidth

        if (rect == null) {
            rect = RectF()
        }
        rect!!.left = (width - mRadius * 2) / 2
        rect!!.top = (height - mRadius * 2) / 2

        rect!!.right = rect!!.left + mRadius * 2
        rect!!.bottom = rect!!.top + mRadius * 2
        canvas.drawArc(rect!!, 270f, 360f, false, mPaint!!)

        //画进度条
        val mShader = SweepGradient((width / 2).toFloat(), (height / 2).toFloat(),
                intArrayOf(mColor, mColor, mColor, mColor), null)

        mPaint!!.shader = mShader
        mPaint!!.strokeWidth = strokeWidth//覆盖线的宽度

        val sweepAngle = (max - progress * 1.0f) / max * 360//根据现在值和最大值的百分比计算出弧线现在的度数
        canvas.drawArc(rect!!, mDegree.toFloat(), sweepAngle, false, mPaint!!)
    }

    fun go(start: Int, leave: Int) = go(start,leave,null)

    fun go(start: Int, leave: Int,completeListener:(()->Unit)?) {
        cancelAnim()
        max = 360
        val startD = 360 * start / 100

        mRecordAnimator = ValueAnimator.ofInt(startD, 360)
        mRecordAnimator!!.duration = (if (leave > 0) leave else 0).toLong()
        mRecordAnimator!!.interpolator = LinearInterpolator()
        mRecordAnimator!!.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            progress = value
        }
        mRecordAnimator!!.addListener(object: Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                completeListener?.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

        })
        mRecordAnimator!!.start()
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            cancelAnim()
        }
    }

    fun cancelAnim() {
        if (mRecordAnimator != null) {
            mRecordAnimator!!.removeAllUpdateListeners()
            mRecordAnimator!!.removeAllListeners()
            mRecordAnimator!!.cancel()
            mRecordAnimator = null
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
        val widthSize = (mRadius * 2).toInt() + (if (strokeWidth < mBgStrokeWidth) mBgStrokeWidth else strokeWidth * 2).toInt() + paddingLeft + paddingRight
        setMeasuredDimension(widthSize, widthSize)
    }

}
