package com.module.playways.songmanager.event

import com.module.playways.room.song.model.SongModel

class AddSongEvent(var songModel: SongModel, var from: Int)
