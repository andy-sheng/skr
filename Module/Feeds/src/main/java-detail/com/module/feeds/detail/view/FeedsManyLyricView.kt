package com.module.feeds.detail.view

import android.text.TextUtils
import android.view.View
import android.view.ViewStub
import com.common.log.MyLog
import com.common.rx.RxRetryAssist
import com.common.utils.U
import com.common.view.ExViewStub
import com.component.lyrics.LyricsManager
import com.component.lyrics.widget.AbstractLrcView
import com.component.lyrics.widget.AbstractLrcView.LRCPLAYERSTATUS_PLAY
import com.component.lyrics.widget.ManyLyricsView
import com.module.feeds.R
import com.module.feeds.detail.view.inter.BaseFeedsLyricView
import com.component.busilib.model.FeedSongModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class FeedsManyLyricView(viewStub: ViewStub, var showName: Boolean = false) : ExViewStub(viewStub), BaseFeedsLyricView {

    val TAG = "FeedsManyLyricView"
    var mManyLyricsView: ManyLyricsView? = null
    private var mFeedSongModel: FeedSongModel? = null
    private var mDisposable: Disposable? = null
    private var mIsStart: Boolean = false
    private var shift = 0
    override fun init(parentView: View) {
        mManyLyricsView = mParentView?.findViewById(R.id.many_lyrics_view)
        mManyLyricsView?.spaceLineHeight = U.getDisplayUtils().dip2px(15f).toFloat()
    }

    override fun layoutDesc(): Int {
        return R.layout.feeds_detail_many_lyric_layout
    }

    override fun setSongModel(feedSongModel: FeedSongModel, shift: Int) {
        mFeedSongModel = feedSongModel
        this.shift = shift
    }

    override fun loadLyric() {
        tryInflate()
        showLyric(false)
    }

    override fun playLyric() {
        tryInflate()
        showLyric(true)
    }

    private fun showLyric(play: Boolean) {
        if (mFeedSongModel?.songTpl?.lrcTsReader == null) {
            mFeedSongModel?.songTpl?.lrcTs?.let {
                mDisposable?.dispose()
                mDisposable = LyricsManager
                        .loadStandardLyric(mFeedSongModel!!.songTpl!!.lrcTs,shift)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retryWhen(RxRetryAssist(3, ""))
                        .subscribe( { lyricsReader ->
                            MyLog.w(TAG, "onEventMainThread " + "play")
                            mFeedSongModel?.songTpl?.lrcTsReader = lyricsReader
                            whenReaderLoad(play)
                        },{
                            MyLog.e(TAG,it)
                        })
            }
        } else {
            whenReaderLoad(play)
        }
    }

    private fun whenReaderLoad(play: Boolean) {
        if (mManyLyricsView != null) {
//            mManyLyricsView?.setVisibility(View.VISIBLE)
            mManyLyricsView?.initLrcData()
        }
        mManyLyricsView?.lyricsReader = mFeedSongModel?.songTpl?.lrcTsReader
        if (!TextUtils.isEmpty(mFeedSongModel?.workName) && showName) {
            mManyLyricsView?.setSongName("《${mFeedSongModel?.workName}》")
        }

        if (play) {
            mIsStart = true
            mManyLyricsView?.play(mFeedSongModel?.playCurPos ?: 0)
        } else {
            if (mManyLyricsView?.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLyricsView?.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY) {
                mManyLyricsView?.pause()
            }
        }
    }

    override fun seekTo(duration: Int) {
        mFeedSongModel?.playCurPos = duration
        mManyLyricsView?.seekTo(duration)
    }

    override fun pause() {
        mManyLyricsView?.pause()
    }

    override fun resume() {
        mManyLyricsView?.resume()
    }

    override fun isStart(): Boolean = mIsStart

    override fun stop() {
        mFeedSongModel?.playCurPos = 0
        mManyLyricsView?.seekTo(0)
        mManyLyricsView?.pause()
        mDisposable?.dispose()
        mIsStart = false
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)

    }

    override fun setShowState(visibility: Int) {
        setVisibility(visibility)
    }

    override fun showHalf() {
        mManyLyricsView?.setDownLineNum(0)
        mManyLyricsView?.setUpLineNum(1)
    }

    override fun showWhole() {
        mManyLyricsView?.setDownLineNum(2)
        mManyLyricsView?.setUpLineNum(2)
    }

    override fun destroy() {
        mManyLyricsView?.release()
        mIsStart = false
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        mManyLyricsView?.visibility = visibility
        if (visibility == View.GONE) {
            mManyLyricsView?.pause()
        }
    }
}
