package com.component.busilib.view

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.common.log.MyLog
import com.common.utils.dp
import com.component.busilib.R
import kotlin.random.Random

// 随音乐跳动的条形图
class VoiceChartView : LinearLayout, Runnable {

    val mTag = "VoiceChartView"

    var viewWrappers: Array<ViewWrapper?>? = null

    private var chartCount = 0
    private var chartWidth = 1.dp()
    private var chartHeight = 0
    private var chartMarginLeft = 5
    private var chartBackColor = Color.RED
    private var chartShape = 0
    private var chartDuration = 500

    private var wantStart = false //意图开始
    private var startAnimtor = false  //真正开始
    private var hasInit = false  // 是否已经初始化了

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
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM

        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.VoiceChartView)
        chartCount = typeArray.getInt(R.styleable.VoiceChartView_chartCount, 0)
        chartWidth = typeArray.getDimensionPixelSize(R.styleable.VoiceChartView_chartWidth, 1.dp())
        chartHeight = typeArray.getDimensionPixelSize(R.styleable.VoiceChartView_chartHeight, 0)
        chartMarginLeft = typeArray.getDimensionPixelSize(R.styleable.VoiceChartView_chartMarginLeft, 0)
        chartBackColor = typeArray.getColor(R.styleable.VoiceChartView_chartBackColor, Color.WHITE)
        chartShape = typeArray.getResourceId(R.styleable.VoiceChartView_chartShape, 0)
        chartDuration = typeArray.getInteger(R.styleable.VoiceChartView_chartDuration, 500)
        typeArray.recycle()

        if (chartCount != 0) {
            addChartView()
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (!hasInit) {
            hasInit = true
            chartCount = width / (chartWidth + chartMarginLeft)
            addChartView()
            if (!startAnimtor && wantStart) {
                start()
            }
        }
    }

    private fun addChartView() {
        if (chartCount <= 0) {
            return
        }
        viewWrappers = arrayOfNulls(chartCount)
        for (i in 0 until chartCount) {
            val childView = ImageView(context)
            if (chartShape != 0) {
                childView.setBackgroundResource(chartShape)
            } else {
                childView.setBackgroundColor(chartBackColor)
            }
            val layoutParams = LayoutParams(chartWidth, 100)
            layoutParams.leftMargin = chartMarginLeft
            childView.layoutParams = layoutParams
            addView(childView)
            viewWrappers?.set(i, ViewWrapper(childView))
        }
    }

    private fun startAnimator(viewWrapper: ViewWrapper?, height: Int) {
        viewWrapper?.let {
            viewWrapper.target.clearAnimation()
            ObjectAnimator.ofInt(viewWrapper, "height", height).setDuration(chartDuration.toLong()).start()
        }
    }

    override fun run() {
        if (startAnimtor) {
            start()
        }
    }

    fun start() {
        wantStart = true
        viewWrappers?.let {
            if (it.isNullOrEmpty()) {
                return
            }
            startAnimtor = true
            wantStart = false
            val a = Random
            for (i in 0 until it.size) {
                startAnimator(it[i], a.nextInt(chartHeight))
            }

            removeCallbacks(this)
            postDelayed(this, chartDuration.toLong())
        }
    }

    fun stop() {
        wantStart = false
        startAnimtor = false
        removeCallbacks(this)
        viewWrappers?.forEach {
            it?.target?.clearAnimation()
        }
    }

    fun reset() {
        hasInit = false
        wantStart = false
        startAnimtor = false
        removeCallbacks(this)
        viewWrappers?.forEach {
            it?.target?.clearAnimation()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        reset()
    }

    inner class ViewWrapper(val target: View) {

        fun getWidth(): Int {
            return target.layoutParams.width
        }

        fun setWidth(width: Int) {
            target.layoutParams.width = width
            target.requestLayout()
        }

        fun getHeight(): Int {
            return target.layoutParams.height
        }

        fun setHeight(height: Int) {
            target.layoutParams.height = height
            target.requestLayout()
        }
    }
}