package com.component.lyrics.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

import com.common.log.MyLog
import com.common.utils.U
import com.component.busilib.R
import com.component.lyrics.model.LyricsLineInfo

import java.util.ArrayList

/**
 * 以秒为基准
 * 音阶view
 */
class VoiceScaleView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : View(context, attrs, defStyleAttr) {
    val TAG = "VoiceScaleView"
    internal var hide = false // 隐藏

    private var mReadLineX = 0.2f// 红线大约在距离左边 20% 的位置
    private var mShowReadDot = true
    private var mRedCy: Float = 0.toFloat()
    private var mWidth = -1// view的宽度
    private var mHeight = -1// view的高度
    private var mLocalBeginTs: Long = -1// 本地开始播放的时间戳，本地基准时间
    private var mTranslateTX: Long = 0// 调用方告知的偏移量

    private var mLyricsLineInfoList: List<LyricsLineInfo> = ArrayList() // 歌词

    private lateinit var mLeftBgPaint: Paint
    private lateinit var mRightBgPaint: Paint
    private lateinit var mRedLinePaint: Paint
    private lateinit var mRedOutpaint: Paint
    private lateinit var mRedInnerpaint: Paint
    private lateinit var mLeftPaint: Paint
    private lateinit var mRightPaint: Paint

    private var mLeftBgPaintColor = Color.parseColor("#252736")
    private var mRightBgPaintColor = Color.parseColor("#292B3A")
    private var mRedLinePaintColor = Color.parseColor("#494C62")
    private var mRedOutpaintColor = Color.parseColor("#EF5E85")
    private var mRedInnerpaintColor = Color.parseColor("#CA2C60")
    private var mLeftPaintColor = Color.parseColor("#F5A623")
    private var mRightPaintColor = Color.parseColor("#474A5F")
    private var mShowTopBound = false
    private var mShowBottomBound = false

    var durationProvider: (() -> Long)? = null

    fun setHide(hide: Boolean) {
        this.hide = hide
        if (!this.hide) {
            postInvalidate()
        }
    }

    constructor(context: Context) : this(context, null) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        init(context, attrs)
    }

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.VoiceScaleView)
        mLeftBgPaintColor = typedArray.getColor(R.styleable.VoiceScaleView_leftBgPaintColor, mLeftBgPaintColor)
        mRightBgPaintColor = typedArray.getColor(R.styleable.VoiceScaleView_rightBgPaintColor, mRightBgPaintColor)
        mRedLinePaintColor = typedArray.getColor(R.styleable.VoiceScaleView_redLinePaintColor, mRedLinePaintColor)
        mRedOutpaintColor = typedArray.getColor(R.styleable.VoiceScaleView_redOutpaintColor, mRedOutpaintColor)
        mRedInnerpaintColor = typedArray.getColor(R.styleable.VoiceScaleView_redInnerpaintColor, mRedInnerpaintColor)
        mLeftPaintColor = typedArray.getColor(R.styleable.VoiceScaleView_leftPaintColor, mLeftPaintColor)
        mRightPaintColor = typedArray.getColor(R.styleable.VoiceScaleView_rightPaintColor, mRightPaintColor)
        mShowTopBound = typedArray.getBoolean(R.styleable.VoiceScaleView_showTopBound, false)
        mShowBottomBound = typedArray.getBoolean(R.styleable.VoiceScaleView_showBottomBound, false)

        typedArray.recycle()

        MyLog.e("setLayerType from VoiceScaleView")
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        mLeftBgPaint = com.common.view.ExPaint()
        mLeftBgPaint.color = mLeftBgPaintColor

        mRightBgPaint = com.common.view.ExPaint()
        mRightBgPaint.color = mRightBgPaintColor

        mRedLinePaint = com.common.view.ExPaint()
        mRedLinePaint.color = mRedLinePaintColor

        mRedOutpaint = com.common.view.ExPaint() //外圈
        mRedOutpaint.color = mRedOutpaintColor
        mRedOutpaint.isAntiAlias = true

        mRedInnerpaint = com.common.view.ExPaint() //内圈
        mRedInnerpaint.color = mRedInnerpaintColor
        mRedInnerpaint.isAntiAlias = true

        mLeftPaint = com.common.view.ExPaint()
        mLeftPaint.maskFilter = BlurMaskFilter(U.getDisplayUtils().dip2px(5f).toFloat(), BlurMaskFilter.Blur.SOLID)
        mLeftPaint.color = mLeftPaintColor

        mRightPaint = com.common.view.ExPaint()
        mRightPaint.color = mRightPaintColor

    }

    /**
     * @param lyricsLineInfoList
     * @param translateTX        调用方告知的偏移量
     */
    fun startWithData(lyricsLineInfoList: List<LyricsLineInfo>, translateTX: Int) {
        MyLog.d(TAG, "startWithData lyricsLineInfoList=$lyricsLineInfoList")
        this.mLyricsLineInfoList = lyricsLineInfoList
        this.mLocalBeginTs = -1
        this.mTranslateTX = translateTX.toLong()
        mShowReadDot = true
        postInvalidate()
    }

    fun stop(showReadDot: Boolean) {
        this.mLyricsLineInfoList = ArrayList()
        this.mLocalBeginTs = -1
        this.mTranslateTX = 0
        this.mShowReadDot = showReadDot
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mWidth < 0) {
            mWidth = width
        }
        if (mHeight < 0) {
            mHeight = height
        }
        if (mLocalBeginTs < 0) {
            mLocalBeginTs = System.currentTimeMillis()
        }
        if (hide) {
            return
        }
        val divideLineTX = mReadLineX * mWidth

        initBackground(canvas, divideLineTX)

        var isLowStart = true
        var isRedFlag = false         //标记当前此时歌词是否与圆点重合
        val duration: Long
        duration = if(durationProvider==null){
            System.currentTimeMillis() - mLocalBeginTs
        }else{
            durationProvider?.invoke() ?:0
        }

        for (lyricsLineInfo in mLyricsLineInfoList) {
            val left = divideLineTX + (lyricsLineInfo.startTime.toLong() - mTranslateTX - duration) * SPEED / 1000
            val right = divideLineTX + (lyricsLineInfo.endTime.toLong() - mTranslateTX - duration) * SPEED / 1000
            val h = U.getDisplayUtils().dip2px(7f).toFloat()
            val top = if (isLowStart) height * 2 / 3 - h / 2 else height * 1 / 3 - h / 2
            val bottom = top + h
            if (right < left) {
                MyLog.w(TAG, "right<left? error")
                continue
            }

            if (right <= divideLineTX) {
                val rectF = RectF()
                rectF.left = left
                rectF.right = right
                rectF.top = top
                rectF.bottom = bottom
                canvas.drawRoundRect(rectF, U.getDisplayUtils().dip2px(10f).toFloat(), U.getDisplayUtils().dip2px(10f).toFloat(), mLeftPaint)
            } else if (left < divideLineTX && right > divideLineTX) {
                val rectLeftF = RectF()
                rectLeftF.left = left
                rectLeftF.right = divideLineTX
                rectLeftF.top = top
                rectLeftF.bottom = bottom
                canvas.drawRoundRect(rectLeftF, U.getDisplayUtils().dip2px(10f).toFloat(), U.getDisplayUtils().dip2px(10f).toFloat(), mLeftPaint)
                val rectRightF = RectF()
                rectRightF.left = divideLineTX
                rectRightF.right = right
                rectRightF.top = top
                rectRightF.bottom = bottom
                canvas.drawRoundRect(rectRightF, U.getDisplayUtils().dip2px(10f).toFloat(), U.getDisplayUtils().dip2px(10f).toFloat(), mRightPaint)
                isRedFlag = true
                mRedCy = (top + bottom) / 2
            } else if (left >= divideLineTX) {
                val rectF = RectF()
                rectF.left = left
                rectF.right = right
                rectF.top = top
                rectF.bottom = bottom
                canvas.drawRoundRect(rectF, U.getDisplayUtils().dip2px(10f).toFloat(), U.getDisplayUtils().dip2px(10f).toFloat(), mRightPaint)
            }
            isLowStart = !isLowStart
        }

        if (!isRedFlag) {
            mRedCy = height / 2.0f
        }
        if (mShowReadDot) {
            canvas.drawCircle(divideLineTX, mRedCy, U.getDisplayUtils().dip2px(9f).toFloat(), mRedOutpaint)
            canvas.drawCircle(divideLineTX, mRedCy, U.getDisplayUtils().dip2px(6f).toFloat(), mRedInnerpaint)
        }

        if (!mLyricsLineInfoList.isEmpty()) {
            val last = mLyricsLineInfoList[mLyricsLineInfoList.size - 1]
            if (divideLineTX / SPEED * 1000 + last.endTime - mTranslateTX.toFloat() - duration.toFloat() > 0) {
                // 还能画，让时间继续流逝
                postInvalidateDelayed(30)
            }
        }

    }

    private fun initBackground(canvas: Canvas, divideLineTX: Float) {
        val leftBgF = RectF()
        leftBgF.left = 0f
        leftBgF.right = divideLineTX
        leftBgF.top = 0f
        leftBgF.bottom = mHeight.toFloat()
        canvas.drawRect(leftBgF, mLeftBgPaint)

        val rightBgF = RectF()
        rightBgF.left = divideLineTX
        rightBgF.right = mWidth.toFloat()
        rightBgF.top = 0f
        rightBgF.bottom = mHeight.toFloat()
        canvas.drawRect(rightBgF, mRightBgPaint)

        val redLineF = RectF()
        redLineF.left = divideLineTX - U.getDisplayUtils().dip2px(1f) / 2
        redLineF.right = divideLineTX + U.getDisplayUtils().dip2px(1f) / 2
        redLineF.top = 0f
        redLineF.bottom = mHeight.toFloat()
        canvas.drawRect(redLineF, mRedLinePaint)

        if (mShowTopBound) {
            val topBound = RectF()
            topBound.left = 0f
            topBound.right = mWidth.toFloat()
            topBound.top = 0f
            topBound.bottom = 1f
            canvas.drawRect(topBound, mRedLinePaint)
        }

        if (mShowBottomBound) {
            val bottomBound = RectF()
            bottomBound.left = 0f
            bottomBound.right = mWidth.toFloat()
            bottomBound.top = (mHeight - 1).toFloat()
            bottomBound.bottom = mHeight.toFloat()
            canvas.drawRect(bottomBound, mRedLinePaint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            stop(false)
        }
    }

    companion object {
        internal val SPEED = U.getDisplayUtils().dip2px(72f)// 每秒走72个像素单位
    }

}
