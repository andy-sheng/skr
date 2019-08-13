package com.module.feeds.detail.view

import android.text.TextUtils
import android.view.View
import android.view.ViewStub
import android.widget.ScrollView
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ExViewStub
import com.common.view.ex.ExTextView
import com.component.lyrics.LyricsManager
import com.component.lyrics.LyricsReader
import com.module.feeds.R
import com.module.feeds.detail.view.inter.BaseFeedsLyricView
import com.module.feeds.watch.model.FeedSongModel
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*

class AutoScrollLyricView(viewStub: ViewStub) : ExViewStub(viewStub), BaseFeedsLyricView {
    val TAG = "AutoScrollLyricView"
    var lyricTv: ExTextView? = null
    var scrollView: ScrollView? = null
    var scrollTime: Long? = null
    var mFeedSongModel: FeedSongModel? = null
    var mIsStart: Boolean = false
    var mDisposable: Disposable? = null
    var mScrollJob: Job? = null
    private var shift = 0

    override fun init(parentView: View?) {
        lyricTv = parentView!!.findViewById(R.id.lyric_tv)
        scrollView = parentView!!.findViewById(R.id.scroll_view)
    }

    override fun layoutDesc(): Int {
        return R.layout.auto_scroll_lyric_view_layout
    }

    override fun setSongModel(feedSongModel: FeedSongModel,shift:Int) {
        mFeedSongModel = feedSongModel
        this.shift = shift
    }

    override fun loadLyric() {
        MyLog.d(TAG, "loadLyric")
        tryInflate()
        lyricTv?.text = "正在加载"
        if (TextUtils.isEmpty(mFeedSongModel?.songTpl?.lrcTxtStr)) {
            mDisposable?.dispose()
            fetchLyric {
                whenLoadLyric(false)
            }
        } else {
            whenLoadLyric(false)
        }
    }

    private fun fetchLyric(call: () -> Unit) {
        mDisposable?.dispose()
        if (!TextUtils.isEmpty(mFeedSongModel?.songTpl?.lrcTxt)) {
            mDisposable = LyricsManager
                    .loadGrabPlainLyric(mFeedSongModel?.songTpl?.lrcTxt)
                    .subscribe({ s ->
                        mFeedSongModel?.songTpl?.lrcTxtStr = s
                        call.invoke()
                    }, { throwable -> MyLog.e(TAG, "accept throwable=$throwable") })
        } else {
            mDisposable = LyricsManager
                    .loadStandardLyric(mFeedSongModel!!.songTpl!!.lrcTs, shift)
                    .subscribe({
                        MyLog.w(TAG, "onEventMainThread " + "play")
                        lrcToTxtStr(it)
                        call.invoke()
                    }, { throwable ->
                        MyLog.e(TAG, "accept throwable=$throwable")
                    })
        }
    }

    fun lrcToTxtStr(lrcTsReader: LyricsReader?) {
        lrcTsReader?.let {
            mFeedSongModel?.songTpl?.lrcTxtStr = ""
            it.lrcLineInfos.forEach {
                mFeedSongModel?.songTpl?.lrcTxtStr = mFeedSongModel?.songTpl?.lrcTxtStr + it.value.lineLyrics + "\n"
            }
        }
    }

    override fun playLyric() {
        MyLog.d(TAG, "playLyric")
        tryInflate()
        mIsStart = true

        if (TextUtils.isEmpty(mFeedSongModel?.songTpl?.lrcTxtStr)) {
            mDisposable?.dispose()
            fetchLyric {
                whenLoadLyric(true)
            }
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
        lyricTv?.text = l
        if (play) {
            startScroll()
        } else {
            scrollToTs(mFeedSongModel?.playCurPos ?: 0)
        }
    }

    override fun seekTo(pos: Int) {
        mFeedSongModel?.playCurPos = pos
        scrollToTs(mFeedSongModel?.playCurPos ?: 0)
    }

    override fun isStart(): Boolean = mIsStart

    override fun stop() {
        //MyLog.d(TAG, "stop")
        mIsStart = false
        mFeedSongModel?.playCurPos = 0
        scrollTime = 0
        scrollView?.smoothScrollTo(0, 0)
        mScrollJob?.cancel()
        mDisposable?.dispose()
    }

    override fun onViewDetachedFromWindow(v: View?) {
        super.onViewDetachedFromWindow(v)
        mScrollJob?.cancel()
    }

    private fun startScroll() {
        MyLog.d(TAG, "startScroll")
        mScrollJob?.cancel()
        mScrollJob = GlobalScope.launch(Dispatchers.Main) {
            repeat(Int.MAX_VALUE) {
                val ts = System.currentTimeMillis()
                delay(30)
                scrollToTs(mFeedSongModel?.playCurPos ?: 0)
                mFeedSongModel?.playCurPos = (mFeedSongModel?.playCurPos
                        ?: 0) + (System.currentTimeMillis() - ts).toInt()
            }
        }
    }

    private fun scrollToTs(passTime: Int) {
        val Y = (lyricTv!!.height - U.getDisplayUtils().dip2px(74f)) * (passTime.toDouble() / mFeedSongModel!!.playDurMs!!.toDouble())
        //MyLog.w(TAG, "Y is $Y, passTime is $passTime, duraions is ${mFeedSongModel!!.playDurMs!!.toDouble()}")
        //lyricTv.scrollTo(0, Y.toInt())
        // 要用父布局滚 不然setText就滚动0了 之前的白滚了
        scrollView?.smoothScrollTo(0, Y.toInt())
    }

    override fun destroy() {
        mScrollJob?.cancel()
    }

    override fun pause() {
        MyLog.d(TAG, "pause")
        mScrollJob?.cancel()
    }

    override fun resume() {
        if (mScrollJob?.isActive ?: false) {
            // 如果没取消
            MyLog.d(TAG, "resume状态")
        } else {
            // 如果取消了
            startScroll()
        }
    }

    override fun showHalf() {
        scrollView?.layoutParams?.height = U.getDisplayUtils().dip2px(38f)
    }

    override fun showWhole() {
        scrollView?.layoutParams?.height = U.getDisplayUtils().dip2px(74f)
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            pause()
        }
    }
}