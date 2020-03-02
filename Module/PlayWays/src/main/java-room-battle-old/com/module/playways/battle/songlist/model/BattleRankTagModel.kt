package com.module.playways.battle.songlist.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class BattleRankTagModel : Serializable {
    @JSONField(name = "tabDesc")
    var tabDesc: String = ""
    @JSONField(name = "tabType")
    var tabType: Int = 0
}