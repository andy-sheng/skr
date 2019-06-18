package com.module.playways.doubleplay

import com.module.playways.room.song.model.SongModel

import java.io.Serializable

class DoubleRoundInfo : Serializable {
    var roundSeq: Int = 0
        protected set
    var songModel: SongModel? = null
        internal set

    fun update(roundInfo: DoubleRoundInfo) {

    }
}
