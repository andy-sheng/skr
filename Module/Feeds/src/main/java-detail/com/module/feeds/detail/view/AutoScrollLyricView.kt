package com.module.feeds.detail.view

import android.text.TextUtils
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
    val TAG = "AutoScrollLyricView"
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
        mFeedSongModel = feedSongModel
        passTime = 0
    }

    override fun loadLyric() {
        MyLog.d(TAG, "loadLyric")
        tryInflate()
        lyricTv.text = "正在加载"
        if (TextUtils.isEmpty(mFeedSongModel?.songTpl?.lrcTxtStr)) {
            mDisposable = LyricsManager.getLyricsManager(U.app())
                    .loadGrabPlainLyric(mFeedSongModel?.songTpl?.lrcTxt)
                    .subscribe({ s ->
                        mFeedSongModel?.songTpl?.lrcTxtStr = s
                        whenLoadLyric()
                    }, { throwable -> MyLog.e(TAG, "accept throwable=$throwable") })
        } else {
            whenLoadLyric()
        }
    }

    fun whenLoadLyric() {
        lyricTv.text = "\n${mFeedSongModel?.songTpl?.lrcTxtStr}"
        passTime = mFeedSongModel?.playCurPos ?: 0
        scrollToTs(passTime)
    }

    override fun playLyric() {
        MyLog.d(TAG, "playLyric")
        tryInflate()
        passTime = 0
        mIsStart = true

        if (TextUtils.isEmpty(mFeedSongModel?.songTpl?.lrcTxtStr)) {
            mDisposable = LyricsManager.getLyricsManager(U.app())
                    .loadGrabPlainLyric(mFeedSongModel?.songTpl?.lrcTxt)
                    .subscribe({ s ->
                        mFeedSongModel?.songTpl?.lrcTxtStr = s
                        lyricTv.text = "\n${s}"
                        visibility = View.VISIBLE
                        startScroll()
                    }, { throwable -> MyLog.e(TAG, "accept throwable=$throwable") })
        } else {
            lyricTv.text = "\n${mFeedSongModel?.songTpl?.lrcTxtStr}"
            visibility = View.VISIBLE
            startScroll()
        }
    }

    override fun seekTo(pos: Int) {
        MyLog.d(TAG, "startScroll")
        passTime = pos
    }

    override fun isStart(): Boolean = mIsStart

    override fun stop() {
        MyLog.d(TAG, "stop")
        mIsStart = false
        passTime = 0
        scrollTime = 0
        lyricTv.scrollTo(0, 0)
        mHandlerTaskTimer?.dispose()
    }

    private fun startScroll() {
        MyLog.d(TAG, "startScroll")
        mHandlerTaskTimer?.dispose()
        mHandlerTaskTimer = HandlerTaskTimer
                .newBuilder()
                .interval(30)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(t: Int) {
                        scrollToTs(passTime)
                        passTime = passTime.plus(30)
                    }
                })
    }

    private fun scrollToTs(passTime: Int) {
        val Y = (lyricTv.height - scrollView.height) * (passTime.toDouble() / mFeedSongModel!!.playDurMs!!.toDouble())
        MyLog.w(TAG, "Y is $Y, passTime is $passTime, duraions is ${mFeedSongModel!!.playDurMs!!.toDouble()}")
        //lyricTv.scrollTo(0, Y.toInt())
        // 要用父布局滚 不然setText就滚动0了 之前的白滚了
        scrollView.scrollTo(0,Y.toInt())
    }

    override fun destroy() {
        mHandlerTaskTimer?.dispose()
    }

    override fun pause() {
        MyLog.d(TAG, "pause")
        mHandlerTaskTimer?.dispose()
    }

    override fun resume() {
        startScroll()
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            pause()
        }
    }
}