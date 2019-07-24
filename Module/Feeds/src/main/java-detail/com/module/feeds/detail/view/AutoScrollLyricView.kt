package com.module.feeds.detail.view

import android.view.View
import android.view.ViewStub
import android.widget.ScrollView
import com.common.log.MyLog
import com.common.utils.HandlerTaskTimer
import com.common.view.ExViewStub
import com.common.view.ex.ExTextView
import com.module.feeds.R
import com.module.feeds.detail.view.inter.BaseFeedsLyricView
import com.module.feeds.watch.model.FeedSongModel

class AutoScrollLyricView(viewStub: ViewStub) : ExViewStub(viewStub), BaseFeedsLyricView {
    lateinit var lyricTv: ExTextView
    lateinit var scrollView: ScrollView
    var passTime: Int = 0
    var scrollTime: Long? = null
    var mFeedSongModel: FeedSongModel? = null
    var mIsStart: Boolean = false

    var mHandlerTaskTimer: HandlerTaskTimer? = null

    override fun init(parentView: View?) {
        lyricTv = parentView!!.findViewById(R.id.lyric_tv)
        scrollView = parentView!!.findViewById(R.id.scroll_view)
    }

    override fun layoutDesc(): Int {
        return R.layout.auto_scroll_lyric_view_layout
    }

    override fun setSongModel(feedSongModel: FeedSongModel) {
        tryInflate()
        mFeedSongModel = feedSongModel
        passTime = 0
        lyricTv.text = "如果那两个字没有颤抖\n" +
                "我不会发现我难受\n" +
                "怎么说出口\n" +

                "也不过是分手\n" +

                "如果对于明天没有要求\n" +

                "牵牵手就像旅游\n" +

                "成千上万个门口\n" +

                "总有一个人要先走\n" +

                "怀抱既然不能逗留\n" +

                "何不在离开的时候\n" +
                "一边享受 一边泪流\n" +

                "十年之前\n" +

                "我不认识你 你不属于我\n" +

                "我们还是一样\n" +

                "陪在一个陌生人左右\n" +

                "走过渐渐熟悉的街头\n" +

                "十年之后\n" +

                "我们是朋友 还可以问候\n" +

                "只是那种温柔\n" +

                "再也找不到拥抱的理由\n" +

                "情人最后难免沦为朋友\n" +

                "怀抱既然不能逗留\n" +

                "何不在离开的时候\n" +

                "一边享受 一边泪流\n" +

                "十年之前\n" +

                "我不认识你 你不属于我\n" +

                "我们还是一样\n" +

                "陪在一个陌生人左右\n" +

                "走过渐渐熟悉的街头\n" +

                "十年之后\n" +

                "我们是朋友 还可以问候\n" +

                "只是那种温柔\n" +

                "再也找不到拥抱的理由\n" +

                "情人最后难免沦为朋友\n" +

                "直到和你做了多年朋友\n" +

                "才明白我的眼泪\n" +

                "不是为你而流\n" +

                "也为别人而流\n" +
                "\n"
    }

    override fun playLyric() {
        passTime = 0
        mIsStart = true
        startScroll()
    }

    override fun seekTo(duration: Int) {
//        MyLog.w("AutoScrollLyricView", "passTime - duration is ${passTime - duration}")
//        if (Math.abs(passTime - duration) > 100) {
//            passTime = duration
//        }
    }

    override fun isStart(): Boolean = mIsStart

    override fun stop() {
        mIsStart = false
        passTime = 0
        scrollTime = 0
        lyricTv.scrollTo(0, 0)
        mHandlerTaskTimer?.dispose()
    }

    private fun startScroll() {
        mHandlerTaskTimer?.dispose()
        mHandlerTaskTimer = HandlerTaskTimer
                .newBuilder()
                .interval(30)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(t: Int) {
                        passTime = passTime.plus(30)
                        val Y = (lyricTv.height - scrollView.height) * (passTime.toDouble() / mFeedSongModel!!.playDurMs!!.toDouble())
                        MyLog.w("AutoScrollLyricView", "Y is $Y, passTime is $passTime, duraions is ${mFeedSongModel!!.playDurMs!!.toDouble()}")
                        lyricTv.scrollTo(0, Y.toInt())
                    }
                })
    }

    override fun destroy() {
        mHandlerTaskTimer?.dispose()
    }

    override fun pause() {
        mHandlerTaskTimer?.dispose()
    }

    override fun resume() {
        startScroll()
    }
}