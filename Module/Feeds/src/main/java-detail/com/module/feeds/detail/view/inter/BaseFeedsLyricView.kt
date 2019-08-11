package com.module.feeds.detail.view.inter

import com.module.feeds.watch.model.FeedSongModel

interface BaseFeedsLyricView {
    fun setSongModel(mFeedSongModel: FeedSongModel,shift:Int)// 偏移 减去这个值
    fun loadLyric()// 加载歌词
    fun playLyric()// 播放歌词
    fun seekTo(duration: Int)
    fun pause()
    fun resume()
    fun stop()
    fun isStart(): Boolean
    fun destroy()
    fun showHalf()
    fun showWhole()
}