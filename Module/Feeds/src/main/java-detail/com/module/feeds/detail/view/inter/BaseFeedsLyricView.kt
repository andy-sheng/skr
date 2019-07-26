package com.module.feeds.detail.view.inter

import com.module.feeds.watch.model.FeedSongModel

interface BaseFeedsLyricView {
    fun setSongModel(mFeedSongModel: FeedSongModel)
    fun loadLyric()// 加载歌词
    fun playLyric()// 播放歌词
    fun seekTo(duration: Int)
    fun pause()
    fun resume()
    fun stop()
    fun isStart(): Boolean
    fun destroy()
}