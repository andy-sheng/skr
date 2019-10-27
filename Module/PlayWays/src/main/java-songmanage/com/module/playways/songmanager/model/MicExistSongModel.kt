package com.module.playways.songmanager.model

import com.alibaba.fastjson.annotation.JSONField
import com.module.playways.room.song.model.SongModel
import java.io.Serializable

class MicExistSongModel : Serializable {

    companion object {
        const val EUSI_IN_PLAY = 1 //演唱中
        const val EUSI_NEXT_PLAY = 2  //下一首
    }

    @JSONField(name = "music")
    var songModel: SongModel? = null
    @JSONField(name = "peerID")
    var peerID: Int = 0  // 合唱用户ID
    @JSONField(name = "status")
    var status: Int = 0  // 演唱状态
    @JSONField(name = "uniqTag")
    var uniqTag: String? = null  // 歌曲唯一标示
    @JSONField(name = "userID")
    var userID: Int = 0
    @JSONField(name = "wantSingType")
    var wantSingType: Int = 0

    override fun toString(): String {
        return "MicExistSongModel(songModel=$songModel, peerID=$peerID, status=$status, uniqTag=$uniqTag, userID=$userID, wantSingType=$wantSingType)"
    }

}