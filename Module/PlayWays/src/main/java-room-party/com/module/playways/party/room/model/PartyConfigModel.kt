package com.module.playways.party.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.module.playways.grab.room.model.GrabScoreTipMsgModel
import com.zq.live.proto.GrabRoom.QGameConfig
import com.zq.live.proto.GrabRoom.QScoreTipMsg
import com.zq.live.proto.RelayRoom.RelayRoomConfig

import java.io.Serializable
import java.util.ArrayList

class PartyConfigModel : Serializable {
    @JSONField(name = "durationTimeMs")
    var durationTimeMs = 0

    companion object {
    }
}
