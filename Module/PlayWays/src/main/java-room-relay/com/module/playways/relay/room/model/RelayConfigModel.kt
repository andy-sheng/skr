package com.module.playways.relay.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.module.playways.grab.room.model.GrabScoreTipMsgModel
import com.zq.live.proto.GrabRoom.QGameConfig
import com.zq.live.proto.GrabRoom.QScoreTipMsg
import com.zq.live.proto.RelayRoom.RelayRoomConfig

import java.io.Serializable
import java.util.ArrayList

class RelayConfigModel : Serializable {
    @JSONField(name = "durationTimeMs")
    var durationTimeMs = 0

    companion object {

        fun parseFromPB(config: RelayRoomConfig): RelayConfigModel {
            val model = RelayConfigModel()
            model.durationTimeMs = config.durationTimeMs
            return model
        }
    }
}
