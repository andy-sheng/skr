package com.module.feeds.detail.view

import android.view.View
import android.view.ViewStub
import android.widget.ScrollView
import com.common.log.MyLog
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.common.view.ExViewStub
import com.common.view.ex.ExTextView
import com.component.lyrics.LyricsManager
import com.module.feeds.R
import com.module.feeds.detail.view.inter.BaseFeedsLyricView
import com.module.feeds.watch.model.FeedSongModel
import io.reactivex.disposables.Disposable

class AutoScrollLyricView(viewStub: ViewStub) : ExViewStub(viewStub), BaseFeedsLyricView {
    val mTag = "AutoScrollLyricView"
    lateinit var lyricTv: ExTextView
    lateinit var scrollView: ScrollView
    var passTime: Int = 0
    var scrollTime: Long? = null
    var mFeedSongModel: FeedSongModel? = null
    var mIsStart: Boolean = false
    var mDisposable: Disposable? = null

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
        lyricTv.text = "正在加载"
        mDisposable = LyricsManager.getLyricsManager(U.app())
                .loadGrabPlainLyric(feedSongModel.songTpl!!.lrcTxt)
                .subscribe({ s ->
                    lyricTv.text = "\n${s}"
                }, { throwable -> MyLog.e(mTag, "accept throwable=$throwable") })
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