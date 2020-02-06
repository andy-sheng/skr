package com.module.playways.party.create.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable


class PartyCreateRecommendGameModel : Serializable {
    @JSONField(name = "gameDesc")
    var gameDesc: String = ""
    @JSONField(name = "gameMode")
    var gameMode: Int = 0
    @JSONField(name = "playID")
    var playID: Int = 0
    @JSONField(name = "ruleID")
    var ruleID: Int = 0
}

