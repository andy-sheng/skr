package com.module.feeds.detail.manager

import com.common.log.MyLog
import com.module.feeds.watch.model.FeedSongModel

abstract class AbsPlayModeManager {
    open fun changeMode(mode: FeedSongPlayModeManager.PlayMode) {
        MyLog.d("AbsPlayModeManager", "changeModemode = $mode")
    }

    abstract fun getNextSong(userAction: Boolean, callback: (songMode: FeedSongModel?) -> Unit)

    abstract fun getPreSong(userAction: Boolean, callback: (songMode: FeedSongModel?) -> Unit)

    abstract fun playState(isPlaying: Boolean)

    open fun getCurMode(): FeedSongPlayModeManager.PlayMode {
        return FeedSongPlayModeManager.PlayMode.ORDER
    }
}