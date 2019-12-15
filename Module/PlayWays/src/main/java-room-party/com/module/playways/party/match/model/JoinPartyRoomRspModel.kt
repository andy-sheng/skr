package com.module.playways.party.match.model

import com.alibaba.fastjson.annotation.JSONField
import com.module.playways.party.room.model.PartyPlayerInfoModel
import com.module.playways.party.room.model.PartyRoundInfoModel
import com.module.playways.party.room.model.PartySeatInfoModel
import java.io.Serializable

class JoinPartyRoomRspModel : Serializable {
    @JSONField(name = "roomID")
    var roomID = 0

    @JSONField(name = "agoraToken")
    var agoraToken: String? = null

    @JSONField(name = "applyUserCnt")
    var applyUserCnt: Int? = null

    @JSONField(name = "currentRound")
    var currentRound: PartyRoundInfoModel? = null

    @JSONField(name = "gameStartTimeMs")
    var gameStartTimeMs: Long? = null

    @JSONField(name = "notice")
    var notice: String? = null

    @JSONField(name = "onlineUserCnt")
    var onlineUserCnt: Int? = null

    @JSONField(name = "roomName")
    var roomName: String? = null

    @JSONField(name = "seats")
    var seats: ArrayList<PartySeatInfoModel>? = null

    @JSONField(name = "topicName")
    var topicName: String? = null

    @JSONField(name = "users")
    var users: ArrayList<PartyPlayerInfoModel>? = null

    @JSONField(name = "enterPermission")
    var enterPermission = 2

    @JSONField(name = "elapsedTimeMs")
    var elapsedTimeMs: Int? = 0

    //[ RT_UNKNOWN, RT_PERSONAL, RT_FAMILY ]
    @JSONField(name = "roomType")
    var roomType: Int? = 0
}