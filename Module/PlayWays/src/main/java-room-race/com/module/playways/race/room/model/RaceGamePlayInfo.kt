package com.module.playways.race.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.module.playways.room.song.model.SongModel
import com.zq.live.proto.RaceRoom.RaceGameInfo
import java.io.Serializable

class RaceGamePlayInfo : Serializable{
    @JSONField(name = "commonMusic")
    var commonMusic: SongModel? = null
    @JSONField(name = "roundGameType")
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