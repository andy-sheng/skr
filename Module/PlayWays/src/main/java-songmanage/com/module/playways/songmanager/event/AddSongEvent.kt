package com.module.playways.songmanager.event

import com.module.playways.room.song.model.SongModel

class AddSongEvent(songModel: SongModel) {
    var songModel: SongModel
        internal set

    init {
        this.songModel = songModel
    }
}
