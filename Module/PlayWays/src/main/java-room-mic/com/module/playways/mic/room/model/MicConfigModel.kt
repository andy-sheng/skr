package com.module.playways.mic.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.module.playways.grab.room.model.GrabScoreTipMsgModel
import com.zq.live.proto.GrabRoom.QGameConfig
import com.zq.live.proto.GrabRoom.QScoreTipMsg

import java.io.Serializable
import java.util.ArrayList

class MicConfigModel : Serializable{
    @JSONField(name = "maxUserCnt")
    var maxUserCnt = 0
    @JSONField(name = "MScoreTipMsg")
    var qScoreTipMsg: List<GrabScoreTipMsgModel> = ArrayList()
}
