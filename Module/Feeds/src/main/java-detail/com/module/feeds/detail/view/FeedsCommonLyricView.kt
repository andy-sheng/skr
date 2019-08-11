package com.module.feeds.detail.view

import android.text.TextUtils
import android.view.View
import android.view.ViewStub
import com.module.feeds.R
import com.module.feeds.detail.view.inter.BaseFeedsLyricView
import com.module.feeds.watch.model.FeedSongModel

/**
 * 可以播伴奏清唱等多种歌词
 */
class FeedsCommonLyricView(rootView: View) : BaseFeedsLyricView {

    override fun loadLyric() {
        throw Exception("外部只要调用setSongModel就行了")
    }

    val TAG = "FeedsCommonLyricView"
    var mAutoScrollLyricView: AutoScrollLyricView? = null
    var mFeedsManyLyricView: FeedsManyLyricView? = null
    var mFeedSongModel: FeedSongModel? = null
    var mBaseFeedsLyricView: BaseFeedsLyricView? = null

    init {
        run {
            val viewStub = rootView.findViewById<ViewStub>(R.id.auto_scroll_lyric_view_layout_viewstub)
            mAutoScrollLyricView = AutoScrollLyricView(viewStub)
        }

        run {
            val viewStub = rootView.findViewById<ViewStub>(R.id.feeds_many_lyric_layout_viewstub)
            mFeedsManyLyricView = FeedsManyLyricView(viewStub)
            mFeedsManyLyricView?.showHalf()
        }
    }
    override fun setSongModel(feedSongModel: FeedSongModel, shift: Int) {
        mFeedSongModel = feedSongModel
        mFeedsManyLyricView?.setSongModel(feedSongModel,shift)
        mAutoScrollLyricView?.setSongModel(feedSongModel,shift)
        if (!TextUtils.isEmpty(feedSongModel.songTpl?.lrcTs) && feedSongModel.songType == 1) {
            // 只要有伴奏文件，不管清唱和伴奏都是这个view
            mBaseFeedsLyricView = mFeedsManyLyricView
        } else {
            mBaseFeedsLyricView = mAutoScrollLyricView
        }
        mBaseFeedsLyricView?.loadLyric()
    }

    override fun isStart(): Boolean {
        return mBaseFeedsLyricView?.isStart() ?: false
    }

    override fun playLyric() {
        mBaseFeedsLyricView?.playLyric()
    }

    override fun seekTo(duration: Int) {
        mBaseFeedsLyricView?.seekTo(duration)
    }

    override fun showHalf() {
        mBaseFeedsLyricView?.showHalf()
    }

    override fun showWhole() {
        mBaseFeedsLyricView?.showWhole()
    }

    override fun pause() {
        mBaseFeedsLyricView?.pause()
    }

    override fun resume() {
        mBaseFeedsLyricView?.resume()
    }

    override fun stop() {
        mBaseFeedsLyricView?.stop()
    }

    override fun destroy() {
        mBaseFeedsLyricView?.destroy()
    }
}
