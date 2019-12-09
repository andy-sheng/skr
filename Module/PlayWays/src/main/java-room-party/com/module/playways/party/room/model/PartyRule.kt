package com.module.playways.party.room.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class PartyRule : Serializable {
    @JSONField(name = "ruleDesc")
    var ruleDesc: String = ""
    @JSONField(name = "ruleID")
    var ruleID: Int = 0
    @JSONField(name = "ruleName")
    var ruleName: String = ""
    @JSONField(name = "ruleType")
    var ruleType: Int = 0     //PGT_Unknown = 0 : 未知场景 - PGT_Play = 1 : 剧本类游戏 - PGT_Question = 2 : 题库类游戏 - PGT_Free = 3 : 自由主持类游戏 - PGT_KTV = 4 : ktv唱歌类游戏

    enum class RULE_TYPE {
        PGT_Unknown, PGT_Play, PGT_Question, PGT_Free, PGT_KTV


    }
}