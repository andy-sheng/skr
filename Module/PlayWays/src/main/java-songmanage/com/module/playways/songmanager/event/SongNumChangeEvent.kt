package com.module.playways.songmanager.event

class SongNumChangeEvent(songNum: Int) {
    var songNum = -1
        internal set

    init {
        this.songNum = songNum
    }
}
