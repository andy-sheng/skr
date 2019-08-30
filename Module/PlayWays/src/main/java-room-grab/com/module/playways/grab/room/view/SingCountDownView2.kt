package com.module.playways.grab.room.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.common.utils.HandlerTaskTimer
import com.module.playways.R
import com.module.playways.grab.room.view.control.SelfSingCardView
import kotlinx.android.synthetic.main.grab_sing_count_down2_view_layout.view.*

/**
 * 演唱时倒计时
 */
class SingCountDownView2 : RelativeLayout {

    internal var mListener: SelfSingCardView.Listener? = null
    internal var mCounDownTask: HandlerTaskTimer? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        inflate(context, R.layout.grab_sing_count_down2_view_layout, this)
    }

    fun reset() {
        circle_count_down_view.setProgress(0)
        circle_count_down_view.max = 360
        circle_count_down_view.cancelAnim()
        this.mListener = null
        mCounDownTask?.dispose()
    }

    fun setListener(listener: SelfSingCardView.Listener) {
        this.mListener = listener
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
                        mListener?.onSelfSingOver()
                    }
                })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mCounDownTask?.dispose()
    }

}
