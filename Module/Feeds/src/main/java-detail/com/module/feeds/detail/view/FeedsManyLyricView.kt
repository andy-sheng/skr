package com.module.feeds.detail.view

import android.view.View
import android.view.ViewStub

import com.common.view.ExViewStub
import com.module.feeds.R
import com.module.feeds.detail.view.inter.BaseFeedsLyricView
import com.zq.lyrics.widget.ManyLyricsView

class FeedsManyLyricView(viewStub: ViewStub) : ExViewStub(viewStub), BaseFeedsLyricView {
    val TAG = "SelfSingLyricView"
    protected var mManyLyricsView: ManyLyricsView? = null

    override fun init(parentView: View) {
        mManyLyricsView = mParentView.findViewById(R.id.many_lyrics_view)
    }

    override fun layoutDesc(): Int {
        return R.layout.feeds_many_lyric_layout
    }

    override fun seekTo() {

    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun stop() {

    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)

    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {

        }
    }
}
