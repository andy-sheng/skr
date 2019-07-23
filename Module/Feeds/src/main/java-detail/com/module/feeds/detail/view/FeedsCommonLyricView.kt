package com.module.feeds.detail.view

import android.view.View
import android.view.ViewStub

import com.module.feeds.R
import com.module.feeds.detail.view.inter.BaseFeedsLyricView

class FeedsCommonLyricView(rootView: View) : BaseFeedsLyricView {
    val mTag = "FeedsCommonLyricView"
    var mAutoScrollLyricView: AutoScrollLyricView? = null
    var mFeedsManyLyricView: FeedsManyLyricView? = null

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

    override fun seekTo() {

    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun stop() {

    }
}
