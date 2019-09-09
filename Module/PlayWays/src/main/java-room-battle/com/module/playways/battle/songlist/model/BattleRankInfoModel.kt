package com.module.playways.battle.songlist.model

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import java.io.Serializable

class BattleRankInfoModel : Serializable {
    @JSONField(name = "blightCnt")
    var blightCnt: Int = 0
    @JSONField(name = "rankSeq")
    var rankSeq: Int = 0
    @JSONField(name = "starCnt")
    var starCnt: Int = 0

    @JSONField(name = "user")
    var user: UserInfoModel? = null

}