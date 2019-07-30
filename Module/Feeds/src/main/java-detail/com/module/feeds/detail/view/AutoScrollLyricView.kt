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
                        whenLoadLyric(false)
                    }, { throwable -> MyLog.e(TAG, "accept throwable=$throwable") })
        } else {
            whenLoadLyric(false)
        }
    }

    override fun playLyric() {
        MyLog.d(TAG, "playLyric")
        tryInflate()
        mIsStart = true

        if (TextUtils.isEmpty(mFeedSongModel?.songTpl?.lrcTxtStr)) {
            mDisposable = LyricsManager.getLyricsManager(U.app())
                    .loadGrabPlainLyric(mFeedSongModel?.songTpl?.lrcTxt)
                    .subscribe({ s ->
                        mFeedSongModel?.songTpl?.lrcTxtStr = s
                        whenLoadLyric(true)
                    }, { throwable -> MyLog.e(TAG, "accept throwable=$throwable") })
        } else {
            whenLoadLyric(true)
        }
    }


    fun whenLoadLyric(play: Boolean) {
        var l = mFeedSongModel?.songTpl?.lrcTxtStr
        if (!TextUtils.isEmpty(mFeedSongModel?.workName)) {
            l = "《${mFeedSongModel?.workName}》\n ${l}"
        }
        visibility = View.VISIBLE
        lyricTv.text = l
        if (play) {
            startScroll()
        } else {
            scrollToTs(mFeedSongModel?.playCurPos ?: 0)
        }
    }

    override fun seekTo(pos: Int) {
        //MyLog.d(TAG, "seekTo")
        mFeedSongModel?.playCurPos = pos
    }

    override fun isStart(): Boolean = mIsStart

    override fun stop() {
        //MyLog.d(TAG, "stop")
        mIsStart = false
        mFeedSongModel?.playCurPos = 0
        scrollTime = 0
        lyricTv.scrollTo(0, 0)
        mHandlerTaskTimer?.dispose()
    }

    private fun startScroll() {
        //MyLog.d(TAG, "startScroll")
        mHandlerTaskTimer?.dispose()
        mHandlerTaskTimer = HandlerTaskTimer
                .newBuilder()
                .interval(30)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(t: Int) {
                        scrollToTs(mFeedSongModel?.playCurPos ?: 0)
                        mFeedSongModel?.playCurPos = (mFeedSongModel?.playCurPos ?: 0) + 30
                    }
                })
    }

    private fun scrollToTs(passTime: Int) {
        val Y = (lyricTv.height - scrollView.height) * (passTime.toDouble() / mFeedSongModel!!.playDurMs!!.toDouble())
        //MyLog.w(TAG, "Y is $Y, passTime is $passTime, duraions is ${mFeedSongModel!!.playDurMs!!.toDouble()}")
        //lyricTv.scrollTo(0, Y.toInt())
        // 要用父布局滚 不然setText就滚动0了 之前的白滚了
        scrollView.smoothScrollTo(0, Y.toInt())
    }

    override fun destroy() {
        mHandlerTaskTimer?.dispose()
    }

    override fun pause() {
        MyLog.d(TAG, "pause")
        mHandlerTaskTimer?.dispose()
    }

    override fun resume() {
        if (mHandlerTaskTimer?.isDisposed == false) {
            // 如果没取消

        } else {
            // 如果取消了
            startScroll()
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            pause()
        }
    }
}