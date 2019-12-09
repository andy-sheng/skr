package com.module.playways.party.room.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class PartyPlayRule : Serializable {
    @JSONField(name = "playCard")
    var playCard: String = ""
    @JSONField(name = "playContent")
    var playContent: String = ""
    @JSONField(name = "playID")
    var playID: Int = 0
    @JSONField(name = "playName")
    var playName: String = ""
}
