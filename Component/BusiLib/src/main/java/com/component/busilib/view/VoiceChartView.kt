package com.component.busilib.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.common.log.MyLog
import com.common.utils.dp
import com.common.view.ExPaint
import com.component.busilib.R

// 随音乐跳动的条形图
class VoiceChartView : View {

    val mTag = "VoiceChartView"

    private var mWidth = -1// view的宽度
    private var mHeight = -1// view的高度

    private var chartFitWidth = false
    private var chartCount = 0
    private var chartWidth = 1.dp()
    private var chartHeight = 0
    private var chartMarginLeft = 5
    private var chartBackColor = Color.RED
    private var chartDuration = 200

    private var paint: ExPaint = ExPaint()

    private var play = false

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    fun init(context: Context, attrs: AttributeSet?) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.VoiceChartView)
        chartFitWidth = typeArray.getBoolean(R.styleable.VoiceChartView_chartFitWidth, false)
        chartCount = typeArray.getInt(R.styleable.VoiceChartView_chartCount, 0)
        chartWidth = typeArray.getDimensionPixelSize(R.styleable.VoiceChartView_chartWidth, 1.dp())
        chartHeight = typeArray.getDimensionPixelSize(R.styleable.VoiceChartView_chartHeight, 0)
        chartMarginLeft = typeArray.getDimensionPixelSize(R.styleable.VoiceChartView_chartMarginLeft, 0)
        chartBackColor = typeArray.getColor(R.styleable.VoiceChartView_chartBackColor, Color.WHITE)
        chartDuration = typeArray.getInteger(R.styleable.VoiceChartView_chartDuration, 200)
        typeArray.recycle()

        paint.color = chartBackColor
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (chartFitWidth) {
            if (mWidth < 0) {
                mWidth = width
            }
            if (mHeight < 0) {
                mHeight = height
            }
            chartCount = mWidth / (chartWidth + chartMarginLeft)
        }

        for (i in 0 until chartCount) {
            val rectF = RectF()
            rectF.left = (i * (chartWidth + chartMarginLeft)).toFloat()
            rectF.right = rectF.left + chartWidth
            rectF.top = mHeight.toFloat() - (chartHeight - Math.random().toFloat() * chartHeight)
            rectF.bottom = mHeight.toFloat()
            canvas?.drawRoundRect(rectF, 0f, 0f, paint)
        }

        if (play) {
            postInvalidateDelayed((Math.random() * chartDuration / 2 + chartDuration / 2).toLong())
        }
    }

    fun start() {
        play = true
        postInvalidateDelayed(chartDuration.toLong())
    }

    fun stop() {
        play = false

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        play = false
    }
}