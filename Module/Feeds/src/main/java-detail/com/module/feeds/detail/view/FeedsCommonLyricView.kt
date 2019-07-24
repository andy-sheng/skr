package com.module.feeds.detail.view

import android.view.View
import android.view.ViewStub
import com.component.feeds.model.FeedSongModel
import com.module.feeds.R
import com.module.feeds.detail.view.inter.BaseFeedsLyricView

class FeedsCommonLyricView(rootView: View) : BaseFeedsLyricView {
    val mTag = "FeedsCommonLyricView"
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
        }
    }

    override fun setSongModel(feedSongModel: FeedSongModel) {
        mFeedSongModel = feedSongModel

        if (feedSongModel.songTpl?.lrcType == 2) {
            mBaseFeedsLyricView = mAutoScrollLyricView
        } else {
            mBaseFeedsLyricView = mFeedsManyLyricView
        }

        mBaseFeedsLyricView?.setSongModel(feedSongModel)
    }

    override fun isStart(): Boolean {
        return mBaseFeedsLyricView!!.isStart()
    }

    override fun playLyric() {
        mBaseFeedsLyricView?.playLyric()
    }

    override fun seekTo(duration: Int) {
        mBaseFeedsLyricView?.seekTo(duration)
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
