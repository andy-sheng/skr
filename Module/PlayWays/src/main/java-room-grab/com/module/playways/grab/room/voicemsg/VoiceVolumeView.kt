package com.module.playways.grab.room.voicemsg

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ExPaint

class VoiceVolumeView : View {

    val TAG = "VoiceVolumeView"

    internal var mVoiceLevel = 1 // 默认为1

    internal var maxWith = U.getDisplayUtils().dip2px(33f)  // 最长宽度
    internal var minWith = U.getDisplayUtils().dip2px(10f)  // 最小宽度

    internal var height = U.getDisplayUtils().dip2px(3f)    // 高度
    internal var marginHight = U.getDisplayUtils().dip2px(3f)  // 间距离

    internal var mBgPaint: Paint = ExPaint()
    internal var mVolumePaint: Paint = ExPaint()

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        mBgPaint.color = Color.parseColor("#474750")
        mVolumePaint.color = Color.WHITE
    }

    fun setVoiceLevel(level: Int) {
        MyLog.d(TAG, "setVoiceLevel level=$level")
        mVoiceLevel = level
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        MyLog.d(TAG,"onDraw")
        super.onDraw(canvas)
        for (i in 0..8) {
            val rectF = RectF()
            rectF.left = 0f
            rectF.right = rectF.left + ((maxWith - minWith) / 8 * i).toFloat() + minWith.toFloat()
            rectF.bottom = (getHeight() - i * (height + marginHight)).toFloat()
            rectF.top = rectF.bottom - height
            if (mVoiceLevel >= i + 1) {
                canvas.drawRoundRect(rectF, 0f, 0f, mVolumePaint)
            } else {
                val rectF = RectF()
                canvas.drawRoundRect(rectF, 0f, 0f, mBgPaint)
            }

        }
    }
}
