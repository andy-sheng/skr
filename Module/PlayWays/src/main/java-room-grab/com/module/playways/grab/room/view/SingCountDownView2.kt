package com.module.playways.grab.room.view

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.common.utils.HandlerTaskTimer
import com.common.utils.dp
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExRelativeLayout
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.module.playways.R
import com.module.playways.grab.room.view.control.SelfSingCardView
import kotlinx.android.synthetic.main.grab_sing_count_down2_view_layout.view.*

/**
 * 演唱时倒计时
 */
class SingCountDownView2 : RelativeLayout {

    internal var mOverListener: (() -> Unit)? = null
    internal var mCounDownTask: HandlerTaskTimer? = null

    val container: ExConstraintLayout
    val songNameTv: TextView

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inflate(context, R.layout.grab_sing_count_down2_view_layout, this)
        container = this.findViewById(R.id.container)
        songNameTv = this.findViewById(R.id.song_name_tv)
    }

    fun reset() {
        circle_count_down_view.setProgress(0)
        circle_count_down_view.max = 360
        circle_count_down_view.cancelAnim()
//        this.mOverListener = null
        mCounDownTask?.dispose()
    }

    fun setBackColor(color: Int) {
        val drawable = DrawableCreator.Builder()
                .setSolidColor(color)
                .setCornersRadius(12.dp().toFloat())
                .build()
        container.background = drawable
    }

    fun setSongName(songName: String?) {
        songNameTv.text = songName
        songNameTv.visibility = View.VISIBLE
    }

    fun setListener(listener: (() -> Unit)?) {
        this.mOverListener = listener
    }

    fun startPlay(fromProgress: Int, totalMs: Int, playNow: Boolean) {
        circle_count_down_view.go(fromProgress, totalMs)
        if (playNow) {
            startCountDownText(totalMs / 1000)
        } else {
            mCounDownTask?.dispose()
            count_down_tv.text = (totalMs / 1000).toString() + "s"
        }
    }

    private fun startCountDownText(counDown: Int) {
        mCounDownTask?.dispose()
        mCounDownTask = HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take(counDown)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(t: Int) {
                        count_down_tv.text = (counDown - t).toString() + "s"
                    }

                    override fun onComplete() {
                        super.onComplete()
                        mOverListener?.invoke()
                    }
                })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mCounDownTask?.dispose()
    }

}
