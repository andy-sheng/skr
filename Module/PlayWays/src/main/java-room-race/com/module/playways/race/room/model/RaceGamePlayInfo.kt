package com.module.playways.race.room.model

import com.module.playways.room.song.model.SongModel
import com.zq.live.proto.RaceRoom.RaceGameInfo

class RaceGamePlayInfo {
    var commonMusic: SongModel? = null
    var roundGameType = 0
}

fun parseFromGameInfoPB(pb: RaceGameInfo): RaceGamePlayInfo {
    val model = RaceGamePlayInfo()
    model.roundGameType = pb.roundGameType.value
    val songModel = SongModel()
    songModel.parse(pb.commonMusic)
    model.commonMusic = songModel
    return model
}