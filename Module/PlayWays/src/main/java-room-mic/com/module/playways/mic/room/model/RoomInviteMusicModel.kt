package com.module.playways.mic.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.module.playways.room.song.model.SongModel
import com.zq.live.proto.MicRoom.UserMusicDetail
import com.zq.live.proto.RelayRoom.RUserMusicDetail
import java.io.Serializable

// 邀请合唱或者PK的model 以及接唱的model
class RoomInviteMusicModel : Serializable {
    @JSONField(name = "userID")
    var userID: Int = 0
    @JSONField(name = "peerID")
    var peerID: Int = 0
    @JSONField(name = "status")
    var status: Int = 0
    @JSONField(name = "uniqTag")
    var uniqTag: String = ""
    @JSONField(name = "wantSingType")
    var wantSingType: Int = 0
    @JSONField(name = "music")
    var music: SongModel? = null

    companion object {
        internal fun parseFromInfoPB(pb: UserMusicDetail): RoomInviteMusicModel {
            val model = RoomInviteMusicModel()
            model.userID = pb.userID
            model.peerID = pb.peerID
            model.uniqTag = pb.uniqTag
            model.status = pb.status.value
            model.wantSingType = pb.wantSingType.value
            val songModel = SongModel()
            songModel.parse(pb.music)
            model.music = songModel
            return model
        }

        internal fun parseFromInfoPB(pb: RUserMusicDetail): RoomInviteMusicModel {
            val model = RoomInviteMusicModel()
            model.userID = pb.userID
            model.peerID = pb.peerID
            model.uniqTag = pb.uniqTag
            model.status = pb.status.value
//            model.wantSingType = pb.wantSingType.value
            val songModel = SongModel()
            songModel.parse(pb.music)
            model.music = songModel
            return model
        }

    }
}