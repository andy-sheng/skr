package com.module.playways.relay.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.module.playways.grab.room.model.GrabScoreTipMsgModel
import com.zq.live.proto.GrabRoom.QGameConfig
import com.zq.live.proto.GrabRoom.QScoreTipMsg

import java.io.Serializable
import java.util.ArrayList

class RelayConfigModel : Serializable{
    @JSONField(name = "durationTimeMs")
    var durationTimeMs = 0
}
