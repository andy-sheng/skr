package com.module.feeds.detail.manager

import com.module.feeds.watch.model.FeedSongModel

interface IPlayModeManager {
    fun changeMode(mode: FeedSongPlayModeManager.PlayMode)
    fun getNextSong(userAction: Boolean): FeedSongModel?
    fun getPreSong(userAction: Boolean): FeedSongModel?
    fun getCurMode(): FeedSongPlayModeManager.PlayMode
}