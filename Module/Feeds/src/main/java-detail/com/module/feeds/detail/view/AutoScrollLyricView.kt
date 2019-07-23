package com.module.feeds.detail.view

import android.view.View
import android.view.ViewStub
import com.common.utils.HandlerTaskTimer
import com.common.view.ExViewStub
import com.common.view.ex.ExTextView
import com.module.feeds.R
import com.module.feeds.detail.view.inter.BaseFeedsLyricView

class AutoScrollLyricView(viewStub: ViewStub) : ExViewStub(viewStub), BaseFeedsLyricView {
    var lyricTv: ExTextView? = null
    var passTime: Long? = null
    var delayTime: Long? = null
    var scrollTime: Long? = null

    var mHandlerTaskTimer: HandlerTaskTimer? = null

    override fun init(parentView: View?) {
        lyricTv = parentView?.findViewById(R.id.lyric_tv)
    }

    override fun layoutDesc(): Int {
        return R.layout.auto_scroll_lyric_view_layout
    }

    override fun seekTo() {

    }

    override fun stop() {
        lyricTv?.text = ""
        passTime = 0
        delayTime = 0
        scrollTime = 0
    }

    fun play() {
        lyricTv?.text = ""
    }

    private fun startScroll() {
        mHandlerTaskTimer?.dispose()
        mHandlerTaskTimer = HandlerTaskTimer
                .newBuilder()
                .interval(30)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(t: Int) {

                    }
                })
    }

    override fun pause() {

    }

    override fun resume() {

    }
}