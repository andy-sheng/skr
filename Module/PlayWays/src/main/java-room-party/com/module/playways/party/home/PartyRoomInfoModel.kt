package com.module.playways.party.home

import java.io.Serializable
import com.alibaba.fastjson.annotation.JSONField


class PartyRoomInfoModel : Serializable {
    @JSONField(name = "avatarUrl")
    var avatarUrl: String? = null
    @JSONField(name = "gameName")
    var gameName: String? = null
    @JSONField(name = "ownerID")
    var ownerID: Int? = null
    @JSONField(name = "ownerName")
    var ownerName: String? = null
    @JSONField(name = "playerNum")
    var playerNum: Int? = null
    @JSONField(name = "roomID")
    var roomID: Int? = null
    @JSONField(name = "roomName")
    var roomName: String? = null
    @JSONField(name = "topicName")
    var topicName: String? = null
}